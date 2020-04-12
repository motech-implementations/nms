package org.motechproject.nms.api.web;

import io.github.bucket4j.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RateLimitInterceptor implements HandlerInterceptor {
    //
    private final Bucket bucket;

    public RateLimitInterceptor(int capacity) {
        Refill refill = Refill.intervally(capacity, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        System.out.println("Inside pre handle");
        ConsumptionProbe probe = this.bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining",
                    Long.toString(probe.getRemainingTokens()));
            return true;
        }
//
        response.setStatus(429); // 429
        response.addHeader("X-Rate-Limit-Retry-After-Milliseconds",
                Long.toString(TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill())));

        return false;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res,
                           Object handler, ModelAndView model)  throws Exception {
    }

    // Called after rendering the view
    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res,
                                Object handler, Exception ex)  throws Exception {
    }

}
