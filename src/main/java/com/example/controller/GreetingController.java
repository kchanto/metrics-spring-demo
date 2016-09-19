package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.example.service.GreetingService;

import java.util.NoSuchElementException;

@RestController
public class GreetingController {

    @Autowired
    private  GreetingService greetingService;
    
    @Timed(name = "timer")
    @Metered(name = "meter")
    @Counted(name="counter.calls.annotation", monotonic=true, absolute=true )

	@RequestMapping("/greeting/{number}")
	public String getGreeting(@PathVariable int number) {
    	
		return greetingService.getGreeting(number);
	}

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNoSuchElementException(NoSuchElementException e) {
		return e.getMessage();
	}

}
