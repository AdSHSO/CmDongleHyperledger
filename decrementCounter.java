import com.wibu.cm.CodeMeter;
import java.io.IOException; 

public class decrementCounter {

    public final static int LED_NONE  = 0;
    public final static int LED_GREEN = 1;
    public final static int LED_RED   = 2;
    public final static int LED_BOTH  = (LED_GREEN | LED_RED);
    
    public static void ErrorHandler(String a_line, int a_exitCode, long hcmEntry)
    {
        switch(CodeMeter.cmGetLastErrorCode())
        {
            case CodeMeter.CMERROR_NO_ERROR:
                return;
            case CodeMeter.CMERROR_BOX_NOT_FOUND:
                System.err.println(a_line + ": Ein entsprechender CmContainer wurde nicht gefunden.");
                break;
            case CodeMeter.CMERROR_ENTRY_NOT_FOUND:
                System.err.println(a_line + ": Entsprechender Eintrag wurde nicht gefunden.");
                break;
            case CodeMeter.CMERROR_INVALID_HANDLE:
                System.err.println(a_line + ": Handle ungültig! Wurde der CmDongle entfernt?");
                break;
            default:
                String acErrText = CodeMeter.cmGetLastErrorText();
                System.err.println(a_line + ": Ein anderer Fehler ist aufgetreten: \"" + acErrText + "\"");
                break;
        }
     // Trotz des Fehlers Versuchen das Handle zu schließen.
        if (0 != hcmEntry)
          CodeMeter.cmRelease(hcmEntry);
        System.exit(a_exitCode);
    }
        
    public static void resetCounter() throws Exception
    {
    	 Runtime run = Runtime.getRuntime();  
         //The best possible I found is to construct a command which you want to execute  
         //as a string and use that in exec. If the batch file takes command line arguments  
         //the command can be constructed a array of strings and pass the array as input to  
         //the exec method. The command can also be passed externally as input to the method.  

         Process p = null;  
         String cmd = "C:\\Users\\hyperledger\\Desktop\\java\\updateCounter.cmd";  
         try {  
        	 p = run.exec(cmd);
        	 p.waitFor();
        	 System.out.println(p.exitValue());  
             System.out.println("RUN.COMPLETED.SUCCESSFULLY");
         }  
         catch (IOException e) {  
             e.printStackTrace();  
             System.out.println("ERROR.RUNNING.CMD");   
         }
         finally {
        	 
        	 p.destroy();
         }
    }
	public static void setLed(int ledValue)
	{
		int flags;
		int res = 0;
		long hcmEntry;
		
		// ACCESS
		CodeMeter.CMCREDENTIAL cmCred = new CodeMeter.CMCREDENTIAL();
		CodeMeter.CMACCESS2 cmAcc = new CodeMeter.CMACCESS2();
		cmAcc.credential = cmCred;
		cmAcc.ctrl |= CodeMeter.CM_ACCESS_NOUSERLIMIT;
		cmAcc.firmCode = 6000010;
		cmAcc.productCode = 99;
		cmAcc.productItemReference = 17;
		hcmEntry = CodeMeter.cmAccess2(CodeMeter.CM_ACCESS_LOCAL, cmAcc);
		if(0 == hcmEntry)
		{
	          ErrorHandler("CmAccess", 1, hcmEntry);
	    }
		
		CodeMeter.CMBOXCONTROL hcmBoxCtrl = new CodeMeter.CMBOXCONTROL();
        CodeMeter.CMPROGRAM_BOXCONTROL hcmPgmBoxCtrl = new CodeMeter.CMPROGRAM_BOXCONTROL();
        
        //Switch flag to specified value
        switch (ledValue)
        {
        case 0:
        	flags = LED_NONE;
        	break;
        case 2:
        	flags = LED_RED;
        	break;
        case 1:
        	flags = LED_GREEN;
        	break;
        case 3: 
        	flags = LED_BOTH;
        	break;
        default:
        	flags = LED_NONE;
        	break;
        }
        CodeMeter.cmGetInfo(hcmEntry, CodeMeter.CM_GEI_BOXCONTROL, hcmBoxCtrl);

        // Behandle jeden Fehler
        // Da das Handle gerade angelegt wurde, sollten keine vorhanden sein.
        if(0 == res)
        {
        	ErrorHandler("CmGetInfo", 2, hcmEntry);
        }

        // Setzen der LED-Informationen auf die gewünschten Werte.
        hcmPgmBoxCtrl.ctrl           = CodeMeter.CM_BC_ABSOLUTE;
        hcmPgmBoxCtrl.indicatorFlags = (short) ((hcmBoxCtrl.indicatorFlags & 0x0fffffffc) | flags);
        CodeMeter.cmProgram(hcmEntry, CodeMeter.CM_GF_SET_BOXCONTROL, hcmPgmBoxCtrl, null);

        // Behandle jeden Fehler
        // Da das Handle gerade angelegt wurde, sollten keine vorhanden sein.
        if(0 == res)
        {
          ErrorHandler("CmProgram", 3, hcmEntry);
        }
        
        res = CodeMeter.cmRelease(hcmEntry);
	}
	public static void main(String[] args)
	{
		//Working
		setLed(3);
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Save error to print
		String ErrorText = "";
		
		// ACCESS
		CodeMeter.CMCREDENTIAL cmCred = new CodeMeter.CMCREDENTIAL();

		CodeMeter.CMACCESS2 cmAcc = new CodeMeter.CMACCESS2();
		cmAcc.credential = cmCred;
		cmAcc.ctrl |= CodeMeter.CM_ACCESS_NOUSERLIMIT;
		cmAcc.firmCode = 6000010;
		cmAcc.productCode = 100;
		cmAcc.productItemReference = 16;
		long hcmse1;
		hcmse1 = CodeMeter.cmAccess2(CodeMeter.CM_ACCESS_LOCAL, cmAcc);
		
		//Get last error as code 32 = Unit Counter is zero
		int error = CodeMeter.cmGetLastErrorCode();
		if ( error == 32)
		{
			//TODO set unitcounter to value X
			System.out.println("Counter is zero.");
			setLed(2);
			try {
				resetCounter();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(0 == hcmse1)
		{   //May occur if counter is Zero
			ErrorText = CodeMeter.cmGetLastErrorText();
			System.out.print(ErrorText);
		    // No matching license entry or CmContainer found.
		}
		else
		{   //If no failure
			// Decrement
			CodeMeter.CMBASECRYPT2 cmBaseCrypt = new CodeMeter.CMBASECRYPT2();

			cmBaseCrypt.ctrl |= CodeMeter.CM_CRYPT_FIRMKEY;
			cmBaseCrypt.ctrl |= CodeMeter.CM_CRYPT_AES;
			cmBaseCrypt.encryptionCodeOptions |= 1;
			cmBaseCrypt.encryptionCodeOptions |= CodeMeter.CM_CRYPT_UCCHECK;
			cmBaseCrypt.encryptionCodeOptions |= CodeMeter.CM_CRYPT_ATCHECK;
			cmBaseCrypt.encryptionCodeOptions |= CodeMeter.CM_CRYPT_ETCHECK;
			cmBaseCrypt.encryptionCodeOptions |= CodeMeter.CM_CRYPT_SAUNLIMITED;
			CodeMeter.CMCRYPT2 cmCrypt = new CodeMeter.CMCRYPT2();
			cmCrypt.cmBaseCrypt = cmBaseCrypt;
			byte [] initkey = {
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

			System.arraycopy(initkey, 0, cmCrypt.initKey, 0, CodeMeter.CM_BLOCK_SIZE);

			byte [] pbDest = {
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

			int res = CodeMeter.cmCrypt2(hcmse1, CodeMeter.CM_CRYPT_DIRECT_ENC, cmCrypt, pbDest);

			if(0 == res)
			{
				ErrorText = CodeMeter.cmGetLastErrorText();
				System.out.println(ErrorText);
			    // Some errors occurred.
			}
			else
			{
				System.out.println("Counter was successfully decremented.");
				setLed(1);
			}
			
			//close handle

			res = CodeMeter.cmRelease(hcmse1);

			if(0 == res)
			{
				ErrorText = CodeMeter.cmGetLastErrorText();
				System.out.println(ErrorText);
			    /* Es sind Fehler aufgetreten. */
			}
		}

	}

}