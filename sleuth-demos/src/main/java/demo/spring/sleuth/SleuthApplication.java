package demo.spring.sleuth;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SleuthApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SleuthApplication.class).web(true).run(args);
    }
}
