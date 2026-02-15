package xenosoft.imldintelligence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("xenosoft.imldintelligence.module.**.internal.repository")
public class ImldIntelligenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImldIntelligenceApplication.class, args);
    }

}
