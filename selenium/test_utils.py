
from typing import List
import selenium
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webelement import WebElement

from selenium.webdriver.support import expected_conditions
from selenium.webdriver.support.ui import WebDriverWait

from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.chrome.service import Service as ChromeService

from webdriver_manager.firefox import GeckoDriverManager
from selenium.webdriver.firefox.service import Service as FirefoxService

GenericWebdriver = selenium.webdriver.Chrome

def construct_webdriver (webdriver_name : str, ci_mode_is_enabled : bool) -> GenericWebdriver :

  # TODO https://github.com/SergeyPirogov/webdriver_manager#gh_token
  if webdriver_name == 'firefox' :
    options = selenium.webdriver.FirefoxOptions()
    if ci_mode_is_enabled:
      options.add_argument('--headless')
      options.add_argument('--no-sandbox')

    webdriver = selenium.webdriver.Firefox(
      service = FirefoxService(GeckoDriverManager().install()),
      options = options
    )

  else :
    options = selenium.webdriver.ChromeOptions()
    if ci_mode_is_enabled:
      options.add_argument('--headless')
      options.add_argument('--no-sandbox')

    webdriver = selenium.webdriver.Chrome(
      service = ChromeService(ChromeDriverManager().install()),
      options = options
    )

  webdriver.get('http://localhost:3333')

  return webdriver

def find_element_by_selenium_id (webdriver : GenericWebdriver, selenium_id : str, wait_for_element_to_be_clickable : bool = False) -> WebElement :

  if wait_for_element_to_be_clickable :
    predicate = expected_conditions.element_to_be_clickable((By.CSS_SELECTOR, '[data-selenium-id="' + selenium_id + '"]'))
  else :
    predicate = expected_conditions.presence_of_element_located((By.CSS_SELECTOR, '[data-selenium-id="' + selenium_id + '"]'))

  return WebDriverWait(webdriver, 3, 0.1).until(predicate)

def find_elements_by_selenium_id (webdriver : GenericWebdriver, selenium_id : str) -> List[WebElement] :
  return webdriver.find_elements(By.CSS_SELECTOR, '[data-selenium-id="' + selenium_id + '"]')

def element_count_to_be_equal_to (selenium_id : str, expected_count : int) -> bool :
  return lambda webdriver : len(find_elements_by_selenium_id(webdriver, selenium_id)) == expected_count

def assert_elements_count_is_equal_to (webdriver : GenericWebdriver, selenium_id : str, expected_count : int) -> None :
  WebDriverWait(webdriver, 3, 0.1).until(
    element_count_to_be_equal_to(selenium_id, expected_count),
    'wait_for_elements_count_to_be(webdriver, ' + selenium_id + ', ' + str(expected_count) + ')'
  )

def wait_for_axios_response (webdriver : GenericWebdriver, response_predicate : callable) -> None :

  def wait_for_axios_response_predicate (webdriver : GenericWebdriver) -> bool :
    try:
      next(
        filter(
          lambda axios_response : response_predicate(axios_response),
          webdriver.execute_script('return axiosResponses')
        ))
      return True
    except:
      return False

  WebDriverWait(webdriver, 3, 0.1).until(wait_for_axios_response_predicate)

def create_room (webdriver : GenericWebdriver, platform : str, room_type : str, room_name : str, password : str, host_name : str, location_index : int) -> None :

  # go to /_game/create_room.vue
  find_element_by_selenium_id(webdriver, 'game-action.create-room', True).click()
  
  # fill out the form
  find_element_by_selenium_id(webdriver, 'form-element.location-picker', True).click()
  find_elements_by_selenium_id(webdriver, 'form-element.location-picker.item-content')[location_index].click()

  find_element_by_selenium_id(webdriver, 'form-element.platform-picker.' + platform).click()
  find_element_by_selenium_id(webdriver, 'form-element.room-type.' + room_type).click()
  find_element_by_selenium_id(webdriver, 'form-element.room-name').send_keys(room_name)
  find_element_by_selenium_id(webdriver, 'form-element.password').send_keys(password)

  find_element_by_selenium_id(webdriver, 'form-element.in-game-name').send_keys(host_name)
  find_element_by_selenium_id(webdriver, 'form-element.password').click() # blur in-game-name input

  wait_for_axios_response(
    webdriver,
    response_predicate = lambda axios_response : axios_response['config']['url'].endswith('/api/user/in_game_name?value=' + host_name)
  )

  # accept form
  find_element_by_selenium_id(webdriver, 'form-action.submit', True).click()
  
  # wait for /_game/room/_id.vue redirect and render is complete
  find_element_by_selenium_id(webdriver, 'label.room-name')

def leave_room (webdriver : GenericWebdriver) -> None :
  button = find_element_by_selenium_id(webdriver, 'form-action.leave', True)
  icon = button.find_element(By.TAG_NAME, 'i')
  user_is_host = 'mdi-close' in icon.get_dom_attribute('class')

  button.click()

  if user_is_host :
    assert_that_user_has_been_redirected_to(webdriver, '/create_room')
  else :
    assert_that_user_has_been_redirected_to(webdriver, '/rooms')

def assert_that_user_has_been_redirected_to (webdriver : GenericWebdriver, url : str) -> None :
  WebDriverWait(webdriver, 3, 0.1).until(
    expected_conditions.url_contains(url),
    'assert that user has been redirected to ' + url
  )

def join_room (webdriver : GenericWebdriver, platform : str, room_id : str) -> None :
  # go to /_game/rooms.vue
  find_element_by_selenium_id(webdriver, 'game-action.list-rooms', True).click()

  # select desired platform
  find_element_by_selenium_id(webdriver, 'form-element.platform-picker.' + platform, True).click()

  # go to /_game/room/${room_id}
  find_element_by_selenium_id(webdriver, 'room-link.' + room_id, True).click()

  # and wait until it initializes
  find_element_by_selenium_id(webdriver, 'label.room-name')

def send_chat_message (webdriver : GenericWebdriver, chat_message_content : str) -> None :
  input = find_element_by_selenium_id(webdriver, 'chat.input')
  input.send_keys(chat_message_content)
  input.send_keys(Keys.ENTER)
