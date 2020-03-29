package org.motechproject.nms.imi.web;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.time.Duration;

@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        Refill refill = Refill.intervally(1, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(1, refill).withInitialTokens(1);
        Bucket bucket = Bucket4j.builder().addLimit(limit).build();
        registry.addInterceptor(new RateLimitInterceptor(bucket, 1)).addPathPatterns("/generateTargetFile");

        refill = Refill.intervally(2, Duration.ofMinutes(1));
        limit = Bandwidth.classic(2, refill);
        bucket = Bucket4j.builder().addLimit(limit).build();
        registry.addInterceptor(new RateLimitInterceptor(bucket, 1))
                .addPathPatterns("/cdrFileNotification");
}
