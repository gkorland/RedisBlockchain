package com.redislabs;

import org.apache.commons.codec.digest.DigestUtils;

public class Main {
	
	 public static void main(String... argv) {
		    if (argv.length != 2) {
		      System.out.println("Please supply a partition id");
		      System.exit(1);
		    }
		    new Blockchain(argv[1]).start();
	 }

}
