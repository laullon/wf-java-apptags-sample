package com.mycompany.app;

import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.entities.tracing.sampling.RateSampler;
import com.wavefront.sdk.entities.tracing.sampling.Sampler;
import com.wavefront.sdk.proxy.WavefrontProxyClient;
import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws InterruptedException {
        ApplicationTags appTags = new ApplicationTags.Builder("my_app", "main")
                .tagsFromEnv("my_.*")
                .tagFromEnv("VERSION", "app_version")
                .build();
        
        WavefrontSender sender = new WavefrontProxyClient.Builder("wavefront-proxy.default.svc.cluster.local")
                .metricsPort(2878)
                .tracingPort(2878)
                .flushIntervalSeconds(2)
                .build();

        Sampler rateSampler = new RateSampler(1);

        WavefrontSpanReporter wfSpanReporter = new WavefrontSpanReporter.Builder().
                withSource("wavefront-tracing-example"). // optional nondefault source name
                build(sender);

        Tracer tracer = new WavefrontTracer.Builder(wfSpanReporter, appTags)
                .withSampler(rateSampler)
                .build();

        System.err.println("Running....");

        while (true) {
            Span span = tracer.buildSpan("test").start();
            Thread.sleep((long)(Math.random() * 5000));
            Span span2 = tracer.buildSpan("wait").asChildOf(span).start();
            Thread.sleep((long)(Math.random() * 1000));
            span2.finish();
            span.finish();
            Thread.sleep(5000);
            System.err.println("ping....");
        }
    }
}
