package kr.co.mz.rsocketclient;

import java.time.Duration;
import kr.co.mz.rsocketclient.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
// 현재 클래스를 Shell 명령어로 사용
@ShellComponent
public class RSocketShellClient {

  private Disposable disposable;

  private final RSocketRequester rSocketRequester;

  public RSocketShellClient(RSocketRequester rSocketRequester) {
    this.rSocketRequester = rSocketRequester;
  }

  // 메서드를 Shell 명령어로 실행하기 위한 어노테이션
  @ShellMethod("Send one request. One response will be printed.")
  public void requestResponse() throws InterruptedException {
    log.info("Sending one request. Waiting for one response...");
    // requester 가 서버와 통신하기 위한 엔드 포인트 지정
    Message res = this.rSocketRequester.route("request-response")
        // 서버로 보낼 데이터 지정
        .data(new Message("mega-client", "requester"))
        // 서버로부터 받은 응답 값을 Message 클래스로 받음
        .retrieveMono(Message.class)
        .log()
        // 응답 결과를 동기로 받기 위함
        .block();

    log.info("Received request-response response: {}", res);
  }

  @ShellMethod("Send one request. No response will be returned.")
  public void fireAndForget() throws InterruptedException {
    log.info("Fire-And-Forget. Sending one request. Expect no response (check server log)...");
    this.rSocketRequester.route("fire-and-forget")
        .data(new Message("mega-client", "requester"))
        // 서버로 데이터 전송
        .send()
        .block();
  }

  // stream start
  @ShellMethod("Send one request. Many responses (stream) will be printed.")
  public void stream() {
    // 클라이언트가 서버로 한번 요청 후 서버는 1초 단위로 클라이언트에게 응답한다.
    log.info("Request-Stream. Sending one request. Waiting for unlimited responses (Stop process to quit)...");
    this.disposable = this.rSocketRequester.route("stream")
        .data(new Message("mega-client", "requester"))
        .retrieveFlux(Message.class)
        .log()
        .subscribe();
  }

  // stream stop
  @ShellMethod("Stop streaming messages from the server.")
  public void stop(){
    // shell에 stop을 입력하면 dispose에 담긴 서버의 응답을 멈추게 한다.
    if(null != disposable) disposable.dispose();
  }

  // channel
  @ShellMethod("Stream some settings to the server. Stream of responses will be printed.")
  public void channel(){
    Mono<Duration> setting1 = Mono.just(Duration.ofSeconds(1));
    Mono<Duration> setting2 = Mono.just(Duration.ofSeconds(3)).delayElement(Duration.ofSeconds(5));
    Mono<Duration> setting3 = Mono.just(Duration.ofSeconds(5)).delayElement(Duration.ofSeconds(15));
    Flux<Duration> settings = Flux.concat(setting1, setting2, setting3)
        .doOnNext(d -> log.info("Sending setting for {}-second interval.", d.getSeconds()));

    disposable = this.rSocketRequester.route("channel")
        .data(settings)
        .retrieveFlux(Message.class)
        .log()
        .subscribe();
  }
}
