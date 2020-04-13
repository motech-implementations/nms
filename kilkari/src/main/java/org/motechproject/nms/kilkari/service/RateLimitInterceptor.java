package org.motechproject.nms.kilkari.service;

import io.github.bucket4j.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RateLimitInterceptor implements HandlerInterceptor {
    //
    private final Bucket bucket;
    private final String url = "http://192.168.200.4:8080/NMSReportingSuite/nms/mail/emailAlert";
    private final String api;
    private final String USER_AGENT = "Mozilla/5.0";
    private final int capacity;
    private final String email = "swathi.g@beehyv.com";

    public RateLimitInterceptor(int capacity, String api) {
        this.capacity = capacity;
        Refill refill = Refill.intervally(capacity, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        this.bucket = Bucket4j.builder().addLimit(limit).build();
        this.api = api;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        System.out.println("Inside pre handle");
        ConsumptionProbe probe = this.bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            Long remTokens = probe.getRemainingTokens();
            response.addHeader("X-Rate-Limit-Remaining",
                    Long.toString(remTokens));
            if(remTokens==(0.4*capacity)){
                sendEmailAlert(remTokens);
                System.out.println("sendEmailAlert "+api + remTokens);
            }
            return true;
        }

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

    public void sendEmailAlert(Long remTokens) throws Exception {
        String finalUrl = url + "?api=" + api + "&remTokens=" + remTokens + "email=" + email;
        URL obj = new URL(finalUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + finalUrl);
        System.out.println("Response Code : " + responseCode);
    }

}
