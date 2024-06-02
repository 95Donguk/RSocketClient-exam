package kr.co.mz.rsocketclient.client;

import java.time.Duration;
import kr.co.mz.rsocketclient.dto.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
// 현재 클래스를 Shell 명령어로 사용
@ShellComponent
@RequiredArgsConstructor
public class RSocketShellClient {

  private Disposable disposable;

  private final RSocketClient rSocketClient;

  @ShellMethod("Send one request. One response will be printed.")
  public void requestResponse() throws InterruptedException {
    log.info("Sending one request. Waiting for one response...");
    rSocketClient.requestResponse(new Message("mega-client", "requester"))
        .log()
        .doOnNext(res -> log.info("Received request-response response: {}", res))
        .block();
  }

  @ShellMethod("Send one request. No response will be returned.")
  public void fireAndForget() throws InterruptedException {
    log.info("Fire-And-Forget. Sending one request. Expect no response (check server log)...");
    rSocketClient.fireAndForget(new Message("mega-client", "requester"))
        .log()
        .block();
  }

  // stream start
  @ShellMethod("Send one request. Many responses (stream) will be printed.")
  public void stream() {
    // 클라이언트가 서버로 한번 요청 후 서버는 1초 단위로 클라이언트에게 응답한다.
    log.info(
        "Request-Stream. Sending one request. Waiting for unlimited responses (Stop process to quit)...");
    this.disposable = rSocketClient.stream(new Message("mega-client", "requester"))
        .doOnNext(res -> log.info("Received stream response: {}", res))
        .log()
        .subscribe();
  }

  // stream stop
  @ShellMethod("Stop streaming messages from the server.")
  public void stop() {
    // shell에 stop을 입력하면 dispose에 담긴 서버의 응답을 멈추게 한다.
    if (null != disposable) {
      disposable.dispose();
    }
  }

  // channel
  @ShellMethod("Stream some settings to the server. Stream of responses will be printed.")
  public void channel() {
    Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(1));
    Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(5));
    Mono<Duration> setting3 = Mono.just(Duration.ofSeconds(5)).delayElement(Duration.ofSeconds(15));
    Flux<Duration> settings = Flux.concat(setting1, setting2, setting3)
        .doOnNext(d -> log.info("Sending setting for {}-second interval.", d.getSeconds()));

    disposable = rSocketClient.channel(settings)
        .doOnNext(res -> log.info("Received channel response: {}", res))
        .log()
        .subscribe();
  }
}
