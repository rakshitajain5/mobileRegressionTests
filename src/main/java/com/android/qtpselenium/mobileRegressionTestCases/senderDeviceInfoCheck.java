package com.android.qtpselenium.mobileRegressionTestCases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.appium.java_client.android.AndroidDriver;

@Test
public class senderDeviceInfoCheck implements IRetryAnalyzer {

	AndroidDriver driver = null;
	private int retryCount = 0;
	private int maxRetryCount = 2;

	public boolean retry(ITestResult result) {

		if (retryCount < maxRetryCount) {
			retryCount++;
			return true;
		}
		return false;
	}

	@Parameters({ "Sender", "SenderUdid", "AppPackage", "AppActivity", "SenderPlatform" })
	@BeforeTest(alwaysRun = true)
	public void setup(String deviceName, String udid, String AppPackage, String AppActivity, String platform) {
		try {
			System.out.println("Created Appium Session");

			DesiredCapabilities capabilities = DesiredCapabilities.android();
			capabilities.setCapability("deviceName", deviceName);
			capabilities.setCapability("platformName", platform);
			capabilities.setCapability("appPackage", AppPackage);
			capabilities.setCapability("appActivity", AppActivity);
			capabilities.setCapability("udid", udid);

			driver = new AndroidDriver(new URL("http://127.0.0.1:4725/wd/hub"), capabilities);
			System.out.println("initialised driver");
			Thread.sleep(10000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Parameters({ "SenderUdid", "AaptPath", "ApkPath" })
	@Test(retryAnalyzer = senderDeviceInfoCheck.class)
	public void getDetails(String udid, String aaptPath, String apkPath) {
		try {
			String str = "adb -s " + udid + " shell getprop ro.product.model";
			BufferedReader buffer = adbCommandsExecute.execute(str);
			String result = driver.findElement(By.id("com.reliance.jio.jioswitch:id/deviceNetworkInfo")).getText();
			Assert.assertTrue(result.contains(buffer.readLine()));

			str = "adb -s " + udid + " shell dumpsys netstats | grep -E 'iface=wlan.*networkId'";
			buffer = adbCommandsExecute.execute(str);
			String wifi = buffer.readLine();
			if (wifi != null) {
				wifi = wifi.substring(wifi.indexOf("networkId") + 10, wifi.indexOf("]"));
				Assert.assertTrue(result.contains(wifi));
			} else {
				Assert.assertTrue(result.contains("Not connected to any network"));
			}
			Thread.sleep(2000);

			str = aaptPath + " dump badging " + apkPath + " | grep 'versionName'";
			String[] commands = { "/bin/sh", "-c", str };
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(commands);
			buffer = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String versionName = buffer.readLine();
			versionName = versionName.substring(versionName.indexOf("versionName") + 13,
					versionName.indexOf("platformBuildVersionName") - 2);
			Assert.assertTrue(
					driver.findElement(By.id("com.reliance.jio.jioswitch:id/appInfo")).getText().contains(versionName));

			System.out.println("sender details test passed");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(retryAnalyzer = senderDeviceInfoCheck.class)
	public void termsAndConditionsCheck() {
		try {
			Thread.sleep(5000);
			driver.findElement(By.id("com.reliance.jio.jioswitch:id/tandcLink")).click();
			Assert.assertEquals(driver.findElement(By.id("com.reliance.jio.jioswitch:id/webView")).isDisplayed(), true);
			System.out.println("sender terms and conditions test passed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@AfterTest
	public void closedriver() {
		driver.quit();
	}
}
