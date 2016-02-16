package com.android.qtpselenium.mobileRegressionTestCases;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.appium.java_client.android.AndroidDriver;
import junit.framework.Assert;

@Test
public class wifiInterruptionCheck {
	AndroidDriver senderDriver = null;
	AndroidDriver receiverDriver = null;
	private int retryCount = 0;
	private int maxRetryCount = 2;

	public boolean retry(ITestResult result) {

		if (retryCount < maxRetryCount) {
			retryCount++;
			return true;
		}
		return false;
	}

	@Parameters({ "Sender", "Receiver", "SenderUdid", "ReceiverUdid", "AppPackage", "AppActivity", "SenderPlatform",
			"ReceiverPlatform" })
	@BeforeTest(alwaysRun = true)
	public void setup(String sender, String receiver, String senderUdid, String receiverUdid, String AppPackage,
			String AppActivity, String senderPlatform, String receiverPlatform) {
		try {

			DesiredCapabilities senderCapabilities = DesiredCapabilities.android();
			senderCapabilities.setCapability("deviceName", "Samsung");
			senderCapabilities.setCapability("platformName", "android");
			senderCapabilities.setCapability("appPackage", "com.reliance.jio.jioswitch");
			senderCapabilities.setCapability("appActivity", "com.reliance.jio.jioswitch.ui.SplashActivity");
			senderCapabilities.setCapability("udid", "LRZ5MNHYQ8AAIFD6");
			senderDriver = new AndroidDriver(new URL("http://127.0.0.1:4725/wd/hub"), senderCapabilities);
			System.out.println("App launched on sender");
			Thread.sleep(2000);

			DesiredCapabilities receiverCapabilities = DesiredCapabilities.android();
			receiverCapabilities.setCapability("deviceName", "Samsung");
			receiverCapabilities.setCapability("platformName", "android");
			receiverCapabilities.setCapability("appPackage", "com.reliance.jio.jioswitch");
			receiverCapabilities.setCapability("appActivity", "com.reliance.jio.jioswitch.ui.SplashActivity");
			receiverCapabilities.setCapability("udid", "0123456789ABCDEF");
			receiverDriver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), receiverCapabilities);
			receiverDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			System.out.println("App launched on reciever");
			Thread.sleep(10000);

			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/connectAsSenderButton")).click();
			receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/connectAsReceiverButton")).click();
			System.out.println("click Successful");

			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/connectToAndroidPeerButton")).click();
			receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/connectToAndroidPeerButton")).click();
			WebDriverWait rWait = null;
			rWait = new WebDriverWait(receiverDriver, 15);
			WebDriverWait sWait = null;
			sWait = new WebDriverWait(receiverDriver, 15);
			WebElement webElem = rWait.until(
					ExpectedConditions.visibilityOfElementLocated(By.id("com.reliance.jio.jioswitch:id/instruction")));
			String hotspotText = webElem.getText();
			String hotspotId = hotspotText.substring(hotspotText.lastIndexOf("SNW"));
			System.out.println("connect to hotspot : " + hotspotId + ".");
			Thread.sleep(15000);

			senderDriver.findElement(By.xpath("//android.widget.TextView[@text='" + hotspotId + "']")).click();
			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/continueButton")).click();
			receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/continueButton")).click();
			Thread.sleep(5000);
			webElem = rWait.until(ExpectedConditions
					.visibilityOfElementLocated(By.id("com.reliance.jio.jioswitch:id/largeSizeImage")));
			String imageName = webElem.getAttribute("name");
			System.out.println(imageName);
			senderDriver.findElement(By.name(imageName)).click();
			Thread.sleep(10000);

			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/skipButton")).click();
			Thread.sleep(5000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Parameters({ "SenderUdid" })
	@Test(retryAnalyzer = wifiInterruptionCheck.class)
	public void wifiInterrupt(String senderUdid) {
		try {
			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/startButton")).click();
			String str = "adb -s " + senderUdid
					+ " shell am start -n com.reliance.automationhelper/.MainActivity -e automation wifi -e state false";
			adbCommandsExecute.execute(str);
			Thread.sleep(5000);
			Assert.assertEquals(true, senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/okButton")).isDisplayed());
			Thread.sleep(35000);
			Assert.assertEquals(true, receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/okButton")).isDisplayed());
			System.out.println("wifi Interruption test case passed");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterTest
	public void closedriver() {
		senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/okButton")).click();
		receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/okButton")).click();
		senderDriver.quit();
		receiverDriver.quit();
	}
}
