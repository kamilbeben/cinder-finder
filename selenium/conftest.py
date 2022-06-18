
import pytest

def pytest_addoption (parser):
  parser.addoption("--webdriver", action="store", default="chrome", help="chrome | firefox")
  parser.addoption("--ci-mode", action="store", default="no", help="yes | no")

@pytest.fixture
def webdriver_name (request) -> str :
    return request.config.getoption('--webdriver')

@pytest.fixture
def ci_mode_is_enabled (request) -> bool :
    return request.config.getoption('--ci-mode') == 'yes'
