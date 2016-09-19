package com.example;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.SystemPublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

@Configuration
@EnableMetrics
public class MetricsConfiguringClass extends MetricsConfigurerAdapter {

	@Value("${metrics.graphite.uri:localhost}")
	private String URI;

	@Value("${metrics.graphite.port:2300}")
	private Integer port;

	@Value("${metrics.graphite.prefix:demo}")
	private String prefix;

	@Value("${metrics.graphite.seconds:30}")
	private Long seconds;

	@Override
	public void configureReporters(MetricRegistry metricRegistry) {

		//Spring Boot Actuator Metrics
		exportPublicMetrics(metricRegistry);

		//JVM metric
		addJvm(metricRegistry);

	}

	
	@Bean 
	public ConsoleReporter consoleReporter(MetricRegistry metricRegistry){
		
		final ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
		consoleReporter.start(seconds, TimeUnit.SECONDS);
		return consoleReporter;
		
	}
	
	
	@Bean
	public GraphiteReporter graphiteReporter(MetricRegistry metricRegistry) {

		final Graphite graphite = new Graphite(new InetSocketAddress(URI, port));
		final GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(metricRegistry)
				.prefixedWith(prefix)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.filter(MetricFilter.ALL)
				.build(graphite);
		graphiteReporter.start(seconds, TimeUnit.SECONDS);
		return graphiteReporter;
	}
	

	@Autowired
	private SystemPublicMetrics systemPublicMetrics;

	//Exporting Spring Boot Actuator Metrics
	void exportPublicMetrics(MetricRegistry metricRegistry) {
		for (Metric<?> metric : systemPublicMetrics.metrics()) {
			Counter counter = metricRegistry.counter(metric.getName());
			counter.dec(counter.getCount());
			counter.inc(Double.valueOf(metric.getValue().toString()).longValue());
		}
	}

	//JVM metrics (wrap in MetricSet to add better key prefixes)
	private void addJvm(MetricRegistry metricRegistry) {
		MetricSet jvmMetrics = new MetricSet() {
			@Override
			public Map<String, com.codahale.metrics.Metric> getMetrics() {

				Map<String, com.codahale.metrics.Metric> metrics = new HashMap<String, com.codahale.metrics.Metric>();
				metrics.put("gc", new GarbageCollectorMetricSet());
				metrics.put("file-descriptors", new FileDescriptorRatioGauge());
				metrics.put("memory-usage", new MemoryUsageGaugeSet());
				metrics.put("threads", new ThreadStatesGaugeSet());
				return metrics;
			}
		};
		
		metricRegistry.registerAll(jvmMetrics);

	}

}
