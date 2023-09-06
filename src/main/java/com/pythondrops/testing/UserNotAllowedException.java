package com.pythondrops.testing;

public class UserNotAllowedException extends Exception {
   String message;

   public UserNotAllowedException (String message) {
      this.message = message;
   }

   @Override
   public String getMessage() {
      return this.message;
   }
}
