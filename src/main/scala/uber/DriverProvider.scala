package uber

import java.util

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ChromeOptions, ChromeDriver}
import org.openqa.selenium.firefox.{FirefoxProfile, FirefoxDriver}

/**
 * Author: Kul.
 */
trait DriverProvider {

  def driver: WebDriver
}

class ChromeDriverProvider(downloadDir: String) extends DriverProvider {

  println(getClass.getClassLoader.getResource("chromedriver").getFile)
  System.setProperty("webdriver.chrome.driver", getClass.getClassLoader.getResource("chromedriver").getFile)
  override def driver = new ChromeDriver(options)

  private def options() = {

    val prefMap = new util.HashMap[String, Object]()
    prefMap.put("download.prompt_for_download", "false")
    prefMap.put("download.default_directory", downloadDir)

    val options = new ChromeOptions
    options.setExperimentalOption("prefs", prefMap)
    options.addArguments("--test-type")
    options
  }
}

class FireFoxDriverProvider(downloadDir: String) extends DriverProvider {
  override def driver = new FirefoxDriver(firefoxProfile())

  private def firefoxProfile() = {
    val profile = new FirefoxProfile()
    profile.setPreference("browser.download.folderList", 2)
    profile.setPreference("browser.download.dir", downloadDir)
    profile.setPreference("browser.download.manager.showWhenStarting", false)
    profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf")
    profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream")
    profile
  }

}

