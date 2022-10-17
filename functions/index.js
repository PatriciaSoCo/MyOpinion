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
    if (user?.email?.includes('norpatt_cat@yahoo.com.mx')) {
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
    else if (user?.email?.includes('lapatagorda@hotmail.com')) {
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
  
  export const beforesignedin = beforeUserSignedIn((event) => {
    const user = event.data;
    if (user.email && !user.emailVerified && user.customClaims.emailsent) {
      throw new functions.https.HttpsError(
        'invalid-argument', 'The email needs to be verified before access is granted.');
     }
   });
 export const confirmemailsent = functions.https.onCall(async (data, context) => {
  const auth = admin.auth();
  auth.setCustomUserClaims(context.auth.uid, { emailsent: true })
  .then(() => {
    return 200
    // The new custom claims will propagate to the user's ID token the
    // next time a new one is issued.
  });
});

export const onCreate =  functions.auth.user().onCreate((user, context) => {
  const auth = admin.auth();
  if (user?.email?.includes('norpatt_cat@yahoo.com.mx')) {
    console.log ("admin rol");

      
      const customClaims =  {
       emailsent : false,
        admin: true,
        Host : false,
        User : false,
      }
      return auth.setCustomUserClaims(user.uid, customClaims);
  }
  else if (user?.email?.includes('lapatagorda@hotmail.com')) {
    console.log ("Host rol");
  
     
      const customClaims =  {
        emailsent : false,
        Host: true,
        User: false,
        admin: false,
      }
      return auth.setCustomUserClaims(user.uid, customClaims);
  }
  else { 
    console.log ("User rol");
   
      
      const customClaims = {
        emailsent : false,
        User: true,
        admin: false,
        Host: false,
      }
      return auth.setCustomUserClaims(user.uid, customClaims);
  }
});


   
