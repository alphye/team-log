package com.wiseach.teamlog.web.listeners;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

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
				ResourceBundle bundle = ResourceBundle.getBundle("db",Locale.ENGLISH);
				String[] args=new String[]{"-webPort",bundle.getString("db.webPort"),"-tcpPort",bundle.getString("db.tcpPort")};
				if(bundle.getString("db.webAllowOthers").equals("true")){
					args=Arrays.copyOf(args, args.length+1);
					args[args.length-1]="-webAllowOthers";
				}
				
				if(bundle.getString("db.tcpAllowOthers").equals("true")){
					args=Arrays.copyOf(args, args.length+1);
					args[args.length-1]="-tcpAllowOthers";
				}
				new Console().runTool(args);
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
