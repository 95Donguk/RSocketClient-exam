package kr.co.mz.rsocketclient.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

@Configuration
public class RequestConfig {

  @Bean
  public RSocketRequester getRSocketRequester(RSocketStrategies rSocketStrategies){
    return RSocketRequester.builder()
        // 서버 재연결 설정
        .rsocketConnector(connector -> connector.reconnect(Retry.backoff(10, Duration.ofMillis(500))))
        // 서버 통신에서 객체로 값을 담아 통신하는데 필요한 인코더, 디코더
        .rsocketStrategies(rSocketStrategies)
        // JSON 으로 통신 타입 설정
        .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
        .tcp("localhost", 7000);
  }

}
