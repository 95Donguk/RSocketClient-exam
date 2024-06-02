package kr.co.mz.rsocketclient.client;

import java.time.Duration;
import kr.co.mz.rsocketclient.dto.Message;
import org.springframework.messaging.rsocket.service.RSocketExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RSocketClient {

  @RSocketExchange("request-response")
  Mono<String> requestResponse(Message message) throws InterruptedException;

  @RSocketExchange("fire-and-forget")
  Mono<Void> fireAndForget(Message message) throws InterruptedException;

  @RSocketExchange("stream")
  Flux<String> stream(Message message);

  @RSocketExchange("channel")
  Flux<String> channel(Flux<Duration> settings);

}
