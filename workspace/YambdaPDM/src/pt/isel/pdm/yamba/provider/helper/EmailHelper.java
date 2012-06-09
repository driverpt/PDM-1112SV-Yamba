package pt.isel.pdm.yamba.provider.helper;

import pt.isel.pdm.yamba.model.YambaPost;
import android.app.Activity;
import android.content.Intent;

public class EmailHelper{  
  public static Intent getEmailIntent(String emailSubject, YambaPost status) {
    Intent it = new Intent( Intent.ACTION_SEND );
    it.setType( "message/rfc822" );
    it.putExtra( Intent.EXTRA_SUBJECT, emailSubject);
    it.putExtra( Intent.EXTRA_TEXT, status.getDataForEmail() );
    return it;
  }
}