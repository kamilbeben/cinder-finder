
from time import sleep
from typing import List
from selenium.webdriver.remote.webelement import WebElement
from selenium import webdriver as selenium_webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys

from selenium.webdriver.chrome.service import Service as ChromeService
from webdriver_manager.chrome import ChromeDriverManager


from selenium.webdriver.firefox.service import Service as FirefoxService
from webdriver_manager.firefox import GeckoDriverManager

generic_webdriver = selenium_webdriver.Chrome

def construct_webdriver (webdriver_name : str, ci_mode_is_enabled : bool) -> generic_webdriver :

  print('call construct_webdriver(' + webdriver_name + ', ' + str(ci_mode_is_enabled) + ')')

  # TODO https://github.com/SergeyPirogov/webdriver_manager#gh_token
  if webdriver_name == 'firefox' :
    options = selenium_webdriver.FirefoxOptions()
    if ci_mode_is_enabled:
      options.add_argument('--headless')
      options.add_argument('--no-sandbox')

    webdriver = selenium_webdriver.Firefox(
      service = FirefoxService(GeckoDriverManager().install()),
      options = options
    )

  else :
    options = selenium_webdriver.ChromeOptions()
    if ci_mode_is_enabled:
      options.add_argument('--headless')
      options.add_argument('--no-sandbox')

    webdriver = selenium_webdriver.Chrome(
      service = ChromeService(ChromeDriverManager().install()),
      options = options
    )

  webdriver.implicitly_wait(10)
  webdriver.get('http://localhost:3333')

  return webdriver

def find_element_by_selenium_id (webdriver : generic_webdriver, selenium_id : str) -> WebElement :
  return webdriver.find_element(By.CSS_SELECTOR, '[data-selenium-id="' + selenium_id + '"]')

def find_elements_by_selenium_id (webdriver : generic_webdriver, selenium_id : str) -> List[WebElement] :
  return webdriver.find_elements(By.CSS_SELECTOR, '[data-selenium-id="' + selenium_id + '"]')

def limited_assert_that_predicate_is_true (predicate : callable, description : str) -> None :
  failure_count = 0

  while failure_count < 100 :

    if (predicate()) :
      return

    failure_count += 1
    if failure_count > 100 :
      raise Exception(description + ' ::  has been polled too many times')

    print(description + ' :: does not match predicate yet, sleeping 0.05 seconds')
    sleep(0.05)

def assert_elements_count_is_equal_to (webdriver : generic_webdriver, selenium_id : str, expected_elements_count : int) -> None :
  limited_assert_that_predicate_is_true(
    lambda : len(find_elements_by_selenium_id(webdriver, selenium_id)) == expected_elements_count,
    'wait_for_elements_count_to_be(webdriver, ' + selenium_id + ', ' + str(expected_elements_count) + ')'
  )

def wait_for_axios_response (webdriver : generic_webdriver, response_predicate : callable) -> None :
  
  def wait_for_axios_response_predicate () -> bool :
    try:
      next(
        filter(
          lambda axios_response : response_predicate(axios_response),
          webdriver.execute_script('return axiosResponses')
        ))
      return True
    except:
      return False

  limited_assert_that_predicate_is_true(
    wait_for_axios_response_predicate,
    'wait_for_axios_response'
  )

def create_room (webdriver : generic_webdriver, platform : str, room_type : str, room_name : str, password : str, host_name : str) -> None :

  # go to /_game/create_room.vue
  find_element_by_selenium_id(webdriver, 'game-action.create-room').click()
  
  # fill out the form
  find_element_by_selenium_id(webdriver, 'form-element.location-picker').click()
  find_element_by_selenium_id(webdriver, 'form-element.location-picker.item-content').click()

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
  find_element_by_selenium_id(webdriver, 'form-action.submit').click()
  
  # wait for /_game/room/_id.vue redirect and render is complete
  find_element_by_selenium_id(webdriver, 'label.room-name')

def join_room (webdriver : generic_webdriver, platform : str, room_id : str) -> None :
  # go to /_game/create_room.vue
  find_element_by_selenium_id(webdriver, 'game-action.list-rooms').click()

  # select desired platform
  find_element_by_selenium_id(webdriver, 'form-element.platform-picker.' + platform).click()

  # go to /_game/room/${room_id}
  find_element_by_selenium_id(webdriver, 'room-link.' + room_id).click()

  # and wait until it initializes
  find_element_by_selenium_id(webdriver, 'label.room-name')

def send_chat_message (webdriver : generic_webdriver, chat_message_content : str) -> None :
  input = find_element_by_selenium_id(webdriver, 'chat.input')
  input.send_keys(chat_message_content)
  input.send_keys(Keys.ENTER)
