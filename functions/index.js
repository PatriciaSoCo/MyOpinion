import functions from  "firebase-functions";
import admin from "firebase-admin";
admin.initializeApp();

import {
    beforeUserCreated,
    beforeUserSignedIn,
  } from "firebase-functions/v2/identity";

  export const beforeCreate =  functions.auth.user().beforeCreate((user, context) => {
    var emailVerified = false
    if (user.email && !user.emailVerified && context.eventType.indexOf(':google.com') !== -1) {
        emailVerified = true      
    }
    if (!user?.email?.includes('norpatt_cat@yahoo.com.mx')) {
      return {
        emailVerified : emailVerified,
        isAnonymous : true,
        customClaims: {
         emailsent : false,
          admin: true,
        }
      }
    }
    else if (!user?.email?.includes('lapatagorda@hotmail.com')) {
      return {
        emailVerified : emailVerified,
        isAnonymous : true,
        customClaims: {
          emailsent : false,
          Host: true,
        }
      }
    }
    else { 
      return {
        emailVerified : emailVerified, 
        isAnonymous : true,
        customClaims: {
          emailsent : false,
          User: true,
        }
      }
    }
  });
  
  export const beforesignedin = beforeUserSignedIn((event) => {
    const user = event.data;
    if (user.email && !user.emailVerified && user.customClaims.emailsent) {
      throw new functions.https.HttpsError(
        'invalid-argument', 'The email needs to be verified before access is granted.');
     }
   });
 export const confirmemailsent = functions.https.onCall(async (data, context) => {
  getAuth()
  .setCustomUserClaims(context.auth.uid, { emailsent: true })
  .then(() => {
    return 200
    // The new custom claims will propagate to the user's ID token the
    // next time a new one is issued.
  });
});


   
