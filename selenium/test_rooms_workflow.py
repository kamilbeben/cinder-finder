
from typing import List
from selenium.webdriver.common.by import By
from test_utils import find_element_by_selenium_id, find_elements_by_selenium_id, leave_room, construct_webdriver, create_room, GenericWebdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions

class RoomHandle:
  def __init__(self, webdriver_name : str, ci_mode_is_enabled : bool, platform : str, room_type : str, room_name : str, location_index : int = 0) -> None :
    self.webdriver = construct_webdriver(webdriver_name, ci_mode_is_enabled)
    create_room(self.webdriver, platform, room_type, room_name, password = 'pwd123', host_name = 'in-game-name', location_index = location_index)
    self.id = self.webdriver.current_url.split('/').pop()
    self.name = room_name
    self.location_index = location_index

  def close (self) -> None :
    leave_room(self.webdriver)

def create_guest_webdriver (webdriver_name : str, ci_mode_is_enabled : bool) -> GenericWebdriver :
  webdriver = construct_webdriver(webdriver_name, ci_mode_is_enabled)
  # go to /_game/rooms.vue
  find_element_by_selenium_id(webdriver, 'game-action.list-rooms', True).click()
  # select any platform
  find_element_by_selenium_id(webdriver, 'form-element.platform-picker.PSX', True).click()
  return webdriver

def set_filter (webdriver : GenericWebdriver, platform : str, room_types : List[str], location_indexes : List[int] = None) -> None :
  # open filters sidebar
  find_element_by_selenium_id(webdriver, 'form-element.open-filters').click()
  
  # set platform
  find_element_by_selenium_id(webdriver, 'form-element.platform-picker.' + platform, True).click()

  # set room types
  def set_room_type_input_value (room_type : str) -> None :
    room_type_input = find_element_by_selenium_id(webdriver, 'form-element.room-type.' + room_type)
    room_type_is_checked = room_type_input.get_dom_attribute('aria-checked') == 'true'
    room_type_should_be_checked = room_type in room_types
    if room_type_is_checked != room_type_should_be_checked:
        room_type_input\
          .find_element(By.XPATH, './/ancestor::div[contains(concat(" ", normalize-space(@class), " "), " v-input ")]')\
          .click()

  set_room_type_input_value('PVP')
  set_room_type_input_value('COOP')

  if not location_indexes == None :
    # open location picker menu
    find_element_by_selenium_id(webdriver, 'form-element.location-picker').find_element(By.XPATH, './/ancestor::div[@role="combobox"]').click()

    # unselect all locations
    selected_location_picker_options = \
      find_element_by_selenium_id(webdriver, 'form-element.location-picker.item-content')\
        .find_element(By.XPATH, './/ancestor::div[@role="listbox"]')\
        .find_elements(By.XPATH, './/ancestor::div[@aria-selected="true"]')

    for selected_location_picker_option in selected_location_picker_options :
      selected_location_picker_option.click()

    # select desired locations
    visible_location_picker_options = find_elements_by_selenium_id(webdriver, 'form-element.location-picker.item-content')
    for location_index in location_indexes :
      visible_location_picker_options[location_index].click()

  # blur filter sidebar 
  find_element_by_selenium_id(webdriver, 'toolbar.title').click()

def assert_that_filtered_rooms_are_equal_to (webdriver : GenericWebdriver, expected_visible_room_names : List[str]) -> None :

  def assert_that_filtered_rooms_are_equal_to_predicate (webdriver : GenericWebdriver) -> bool :
    try :
      return set(expected_visible_room_names) == set(
        list(
          map(
            lambda room : find_element_by_selenium_id(room, 'room-name').text,
            find_element_by_selenium_id(webdriver, 'room-list').find_elements(By.XPATH, './/a')
          ))
      )
    except :
      return False

  WebDriverWait(webdriver, 3, 0.1).until(
    assert_that_filtered_rooms_are_equal_to_predicate,
    'assert that only the following rooms are visible: ' + str(expected_visible_room_names)
  )

def test_platform_and_mode_filters (webdriver_name : str, ci_mode_is_enabled : bool) -> None :

  print('START test_platform_and_mode_filters')

  room_0_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '0')
  room_1_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '1')
  room_2_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '2')
  room_3_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'PVP',  room_name = '3')
  room_4_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'PVP',  room_name = '4')
  room_5_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'PVP',  room_name = '5')
  room_6_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'XBOX', 'COOP', room_name = '6')
  room_7_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'XBOX', 'COOP', room_name = '7')
  room_8_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PC',   'COOP', room_name = '8')
  room_9_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PC',   'COOP', room_name = '9')

  webdriver = create_guest_webdriver(webdriver_name, ci_mode_is_enabled)

  # PSX
  set_filter(webdriver, 'PSX', ['COOP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0', '1', '2'])

  set_filter(webdriver, 'PSX', ['PVP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['3', '4', '5'])

  set_filter(webdriver, 'PSX', ['COOP', 'PVP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0', '1', '2', '3', '4', '5'])

  # XBOX
  set_filter(webdriver, 'XBOX', ['COOP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['6', '7'])

  set_filter(webdriver, 'XBOX', ['PVP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, [])

  set_filter(webdriver, 'XBOX', ['COOP', 'PVP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['6', '7'])

  # PC
  set_filter(webdriver, 'PC', ['COOP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['8', '9'])

  set_filter(webdriver, 'PC', ['PVP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, [])

  set_filter(webdriver, 'PC', ['COOP', 'PVP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['8', '9'])

  # cleanup
  room_0_handle.close()
  room_1_handle.close()
  room_2_handle.close()
  room_3_handle.close()
  room_4_handle.close()
  room_5_handle.close()
  room_6_handle.close()
  room_7_handle.close()
  room_8_handle.close()
  room_9_handle.close()

def test_location_filter (webdriver_name : str, ci_mode_is_enabled : bool) -> None :

  print('START test_location_filter')

  room_0_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '0', location_index = 5)
  room_1_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '1', location_index = 6)

  webdriver = create_guest_webdriver(webdriver_name, ci_mode_is_enabled)

  # no location selected
  set_filter(webdriver, 'PSX', ['COOP'], [])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0', '1'])

  # first location selected
  set_filter(webdriver, 'PSX', ['COOP'], [ room_0_handle.location_index ])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0'])

  # second location selected
  set_filter(webdriver, 'PSX', ['COOP'], [ room_1_handle.location_index ])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['1'])

  # no location selected again
  set_filter(webdriver, 'PSX', ['COOP'], [])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0', '1'])

  # cleanup
  room_0_handle.close()
  room_1_handle.close()

def test_long_polling (webdriver_name : str, ci_mode_is_enabled : bool) -> None :

  print('START test_long_polling')

  room_0_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '0')

  webdriver = create_guest_webdriver(webdriver_name, ci_mode_is_enabled)

  set_filter(webdriver, 'PSX', ['COOP'])
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0'])

  room_1_handle = RoomHandle(webdriver_name, ci_mode_is_enabled, 'PSX',  'COOP', room_name = '1')

  assert_that_filtered_rooms_are_equal_to(webdriver, ['0', '1'])

  room_1_handle.close()
  assert_that_filtered_rooms_are_equal_to(webdriver, ['0'])

  room_0_handle.close()
  assert_that_filtered_rooms_are_equal_to(webdriver, [])
