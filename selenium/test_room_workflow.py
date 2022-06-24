
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions
from test_utils import find_element_by_selenium_id, leave_room, construct_webdriver, assert_elements_count_is_equal_to, send_chat_message, create_room, join_room, assert_that_user_has_been_redirected_to

def test_room_workflow (webdriver_name : str, ci_mode_is_enabled : bool) -> None :

  print('START test_room_workflow')

  host_name = 'host_1'
  host_webdriver = construct_webdriver(webdriver_name, ci_mode_is_enabled)
  create_room(host_webdriver, 'PSX', 'COOP', room_name = 'first_room_name', password = 'pwd123', host_name = host_name, location_index = 0)
  room_id = host_webdriver.current_url.split('/').pop()

  guest_1_webdriver = construct_webdriver(webdriver_name, ci_mode_is_enabled)
  join_room(guest_1_webdriver, 'PSX', room_id)
  
  # assert that host name is correct
  assert host_name == find_element_by_selenium_id(guest_1_webdriver, 'label.member.in-game-name').text, 'Host name is not correct'

  guest_2_webdriver = construct_webdriver(webdriver_name, ci_mode_is_enabled)
  join_room(guest_2_webdriver, 'PSX', room_id)

  # assert that host sees two members
  assert_elements_count_is_equal_to(host_webdriver, 'members.entry', 2)

  # as guest_2, leave the room
  find_element_by_selenium_id(guest_2_webdriver, 'form-action.leave').click()
  guest_2_webdriver.quit()

  # assert that host sees one member
  assert_elements_count_is_equal_to(host_webdriver, 'members.entry', 1)

  # chat message counts
  ## 0 -> send message -> 1
  assert_elements_count_is_equal_to(host_webdriver, 'chat.message', 0)
  assert_elements_count_is_equal_to(guest_1_webdriver, 'chat.message', 0)
  
  send_chat_message(guest_1_webdriver, 'first message')
  
  assert_elements_count_is_equal_to(host_webdriver, 'chat.message', 1)
  assert_elements_count_is_equal_to(guest_1_webdriver, 'chat.message', 1)
  
  # 1 -> send message -> 2
  assert_elements_count_is_equal_to(host_webdriver, 'chat.message', 1)
  assert_elements_count_is_equal_to(guest_1_webdriver, 'chat.message', 1)
  
  send_chat_message(host_webdriver, 'second message')

  assert_elements_count_is_equal_to(host_webdriver, 'chat.message', 2)
  assert_elements_count_is_equal_to(guest_1_webdriver, 'chat.message', 2)

  # as host, leave the room
  leave_room(host_webdriver)

  assert_that_user_has_been_redirected_to(guest_1_webdriver, '/rooms')
