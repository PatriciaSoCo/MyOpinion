import functions from  "firebase-functions";
import admin from "firebase-admin";
admin.initializeApp();

import {
    beforeUserCreated,
    beforeUserSignedIn,
  } from "firebase-functions/v2/identity";
import { user } from "firebase-functions/v1/auth";

  export const beforeCreate =  functions.auth.user().beforeCreate((user, context) => {
    var emailVerified = false
    if (user.email && !user.emailVerified && context.eventType.indexOf(':google.com') !== -1) {
        emailVerified = true      
    }
    if (user?.email?.includes('memorf@gmail.com')) {
      console.log ("admin rol");
      return {
        emailVerified : emailVerified,
        customClaims: {
         emailsent : false,
          admin: true,
          Host : false,
          User : false,
        }
      }
    }
    else if (user?.email?.includes('guillermorenteria85@gmail.com')) {
      console.log ("Host rol");
      return {
        emailVerified : emailVerified,
        customClaims: {
          emailsent : false,
          Host: true,
          User: false,
          admin: false,
        }
      }
    }
    else { 
      console.log ("User rol");
      return {
        emailVerified : emailVerified, 
        customClaims: {
          emailsent : false,
          User: true,
          admin: false,
          Host: false,
        }
      }
    }
  });
  
  export const beforeSignIn = functions.auth.user().beforeSignIn((user, context) => {
    
    if (user.email && !user.emailVerified && user.customClaims.emailsent) {
      throw new functions.https.HttpsError(
        'invalid-argument', 'The email needs to be verified before access is granted.');
     }
   });
 export const confirmemailsent = functions.https.onCall(async (data, context) => {
  const auth = admin.auth();
  auth.setCustomUserClaims(context.auth.uid, { emailsent: true, admin: context.auth.token.admin, Host: context.auth.token.Host, User: context.auth.token.User })
  .then(() => {
    return 200
    // The new custom claims will propagate to the user's ID token the
    // next time a new one is issued.
  });
});



   
