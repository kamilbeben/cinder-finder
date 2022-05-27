package pl.beben.ermatchmaker;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class ApplicationSessionListener implements HttpSessionListener {

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    se.getSession().setAttribute("hehe", "abcdefg");
  }

}
