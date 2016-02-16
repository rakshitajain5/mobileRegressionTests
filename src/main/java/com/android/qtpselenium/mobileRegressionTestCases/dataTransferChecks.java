package com.android.qtpselenium.mobileRegressionTestCases;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.appium.java_client.android.AndroidDriver;

@Test
public class dataTransferChecks {

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

	public HashMap<String, String> createList(AndroidDriver senderDriver) {
		HashMap<String, String> hashmap = new HashMap<String, String>();
		for (int i = 0; i < 5; i++) {
			String str1 = senderDriver
					.findElement(By.xpath("//android.widget.ListView/android.widget.LinearLayout[@index='" + i
							+ "']/android.widget.TextView[@index='1']"))
					.getText();
			String str2 = senderDriver
					.findElement(By.xpath("//android.widget.ListView/android.widget.LinearLayout[@index='" + i
							+ "']/android.widget.TextView[@index='2']"))
					.getText();
			hashmap.put(str1, str2);
		}
		return hashmap;
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

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(retryAnalyzer = dataTransferChecks.class)
	public void remindersNotSupported() {
		HashMap<String, String> hashmap = createList(senderDriver);
		Assert.assertEquals(hashmap.get("Reminders"), "Not Supported");
		System.out.println("Remiders not supported test passed");
	}

	@Test(retryAnalyzer = dataTransferChecks.class, dependsOnMethods = { "remindersNotSupported" })
	public void selectiveDataTransfer() {
		senderDriver
				.findElement(By
						.xpath("//android.widget.ListView/android.widget.LinearLayout[@index='2']/android.widget.FrameLayout[@index='0']"))
				.click();
		Assert.assertEquals(senderDriver
				.findElement(By
						.xpath("//android.widget.ListView/android.widget.LinearLayout[@index='2']/android.widget.FrameLayout[@index='0']"))
				.isSelected(), false);
		System.out.println("selective data transfer test passed");
	}

	@Test(retryAnalyzer = dataTransferChecks.class, dependsOnMethods = { "selectiveDataTransfer" })
	public void documentsNotSupported() {
		try {
			/*senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/startButton")).click();
			Thread.sleep(5000);
			
			String perc = senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/progressPerCent")).getText();
			while (!perc.equals("100%"))
			{
				System.out.println("Percentage of transfer ="+perc);
				perc = senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/progressPerCent")).getText();
				System.out.println("Percentage recieved:"+receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/progressPerCent")).getText());
			}

			senderDriver.findElement(By.xpath("//android.widget.Button[@text='CONTINUE']")).click();
			Thread.sleep(5000);*/
			
			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/skipButton")).click();
			Thread.sleep(5000);
			
			HashMap<String, String> hashmap = createList(senderDriver);
			Assert.assertEquals(hashmap.get("Documents"), "Not Supported");
			System.out.println("Documents not supported test passed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(retryAnalyzer = dataTransferChecks.class, dependsOnMethods = { "documentsNotSupported" })
	public void timeEstimateCheck() {
		try {
			String time = senderDriver.findElement(By.xpath("//android.widget.TextView[@index='2']")).getText();
			System.out.println(time);
			String[] lines = time.split("\\n");
			time = lines[1];
			int hours = Integer.parseInt(time.substring(0, 2));
			int minutes = Integer.parseInt(time.substring(3, 5));
			Long estimatedTime = (long) (hours * 60 + minutes);
			System.out.println("estimated time for heavy data transfer = " + estimatedTime);
			senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/startButton")).click();
			Long startTime = System.currentTimeMillis();

			String perc = senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/progressPerCent")).getText();
			while (!perc.equals("100%")) {
				System.out.println("Percentage of transfer =" + perc);
				perc = senderDriver.findElement(By.id("com.reliance.jio.jioswitch:id/progressPerCent")).getText();
				System.out.println("Percentage recieved:"
						+ receiverDriver.findElement(By.id("com.reliance.jio.jioswitch:id/progressPerCent")).getText());
			}

			Long endTime = System.currentTimeMillis();
			System.out
					.println("time taken to complete heavy data transfer " + (endTime - startTime) / 1000 + " seconds");
			Long actualTime = (endTime - startTime) / (1000 * 60);
			//System.out.println("actual time = " + actualTime);
			if (estimatedTime - 3 <= actualTime && estimatedTime + 3 >= actualTime) {
				System.out.println("Heavy data time estimate test passed");
			} else {
				Assert.assertTrue(false);
			}
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@AfterTest
	public void closeDriver() {
		try {
			receiverDriver.findElement(By.xpath("//android.widget.Button[@text='CONTINUE']")).click();
			System.out.println("pressed continue");
			receiverDriver.findElement(By.xpath("//android.widget.Button[@text='TRY AGAIN']")).click();
			System.out.println("pressed try again");
			receiverDriver.findElement(By.xpath("//android.widget.Button[@text='TRY AGAIN']")).click();
			System.out.println("pressed try again");
			receiverDriver.findElement(By.xpath("//android.widget.Button[@text='CONTINUE']")).click();
			System.out.println("pressed continue");

			senderDriver.findElement(By.xpath("//android.widget.Button[@text='EXIT']")).click();
			senderDriver.findElement(By.xpath("//android.widget.Button[@text='YES']")).click();
			System.out.println("exited out of sender device");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			receiverDriver.quit();
			senderDriver.quit();
		}
	}
}
