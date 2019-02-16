package com.wiseach.teamlog.web.listeners;

import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.h2.tools.Console;

public class H2DB implements ServletContextListener{

	private static boolean initialized=false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		startH2();

	}

	public static void startH2(){
		try {
			if(!initialized){
				new Console().runTool(null);
				initialized=true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("================start h2 database===================");
		startH2();
	}
}
