import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.OutputStream;

class Generation {
   Key pub;
   Key priv;
   /**
    * This will create keys for Server and Client 
    */
   public Generation(){
   }

   public void keyMaker(){
      KeyPairGenerator keyGen;
      try {
         keyGen = KeyPairGenerator.getInstance("RSA");
         keyGen.initialize(2048);
         //Creating/Generating a key
         KeyPair key = keyGen.generateKeyPair();
         pub = key.getPublic();
         priv = key.getPrivate();

      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      }     
   }

   public Key getPriv() {
       return priv;
   }
   public Key getPub() {
       return pub;
   }
}
public class KeyGen {

   public static void main(String args[]) throws Exception{
      Generation keys = new Generation();
      keys.keyMaker();
      // Creating a KeyGenerator object

      // Takes public key and creates a file to store the key in it. 
      try{
      File publicKeyFile = new File("server_public.key");
      if(publicKeyFile.createNewFile()) {
         System.out.println("File created: " + publicKeyFile.getName());
      } else {
        System.out.println("File already exists.");
      }
      OutputStream publicOs = new FileOutputStream(publicKeyFile);
      //encodes the key
      publicOs.write(keys.getPub().getEncoded());
      publicOs.close();


      // Takes private key and creates a file to store the key in it.
      File privateKeyFile = new File("server_private.key");
      if(privateKeyFile.createNewFile()) {
         System.out.println("File created: " + privateKeyFile.getName());
      } else {
        System.out.println("File already exists.");
      }
      OutputStream privateOs = new FileOutputStream(privateKeyFile);
      //encodes key
      privateOs.write(keys.getPriv().getEncoded());
      privateOs.close();
      
      }
      catch(IOException e) {
         System.out.println("An error occurred.");
         e.printStackTrace();
      } 
   }
}