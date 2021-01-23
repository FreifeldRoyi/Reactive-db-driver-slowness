package com.example;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;

@QuarkusMain
public class Main implements QuarkusApplication
{
	@Inject ETLRunner runner;

	public static void main(String[] args)
	{
		Quarkus.run(Main.class, args);
	}

	@ActivateRequestContext
	@Override
	public int run(String... args)
	{
		this.runner.run().await().indefinitely();
		return 0;
	}
}
