
import {
    beforeUserCreated,
    beforeUserSignedIn,
  } from "firebase-functions/v2/identity";

  export const beforecreated = beforeUserCreated((event) => {
    const user = event.data;
    if (!user?.email?.includes('norpatt_cat@yahoo.com.mx')) {
      return {
       
        customClaims: {
          admin: true,
        }
      }
        
    }
    else if (!user?.email?.includes('lapatagorda@hotmail.com')) {
      return {
       
        customClaims: {
          Host: true,
        }
      }
    }
    else { 
      return {
       
        customClaims: {
          User: true,
        }
      }
    }
  });
  
  export const beforesignedin = beforeUserSignedIn((event) => {
    const user = event.data;
    if (user.email && !user.emailVerified) {
      throw new HttpsError(
        'invalid-argument', 'The email needs to be verified before access is granted.');
     }
   });



   
