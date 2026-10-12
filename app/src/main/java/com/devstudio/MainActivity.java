package com.devstudio;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.*;
import android.text.*;
import android.widget.*;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(15,18,24), PANEL=Color.rgb(23,27,35), PANEL2=Color.rgb(29,34,44),
        BORDER=Color.rgb(47,55,69), TEXT=Color.rgb(229,234,242), MUTED=Color.rgb(143,154,173),
        BLUE=Color.rgb(104,156,255), GREEN=Color.rgb(94,210,151);
    private LinearLayout root, tree, chatMessages;
    private EditText editor, prompt;
    private TextView fileTitle, status;
    private final LinkedHashMap<String,String> files = new LinkedHashMap<>();
    private String activeFile="MainActivity.java";
    private ExecutorService executor=Executors.newSingleThreadExecutor();
    private android.content.SharedPreferences secrets;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        try {
            MasterKey key=new MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            secrets=EncryptedSharedPreferences.create(this,"devstudio_secrets",key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch(Exception e) { secrets=getSharedPreferences("devstudio_fallback",MODE_PRIVATE); }
        seedFiles(); buildUi();
        if (secrets.getString("api_key", "").isEmpty()) new Handler().postDelayed(this::showApiSetup,350);
    }
    private void seedFiles() {
        files.put("MainActivity.java","package com.example.app;\n\npublic class MainActivity {\n    // Select a file and ask the AI agent to help.\n}\n");
        files.put("AndroidManifest.xml","<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n    <application android:label=\"My App\" />\n</manifest>\n");
        files.put("build.gradle","plugins { id 'com.android.application' }\n\nandroid {\n    namespace 'com.example.app'\n    compileSdk 35\n}\n");
        files.put("README.md","# My Android Project\n\nCreated in DevStudio.\n");
    }
    private GradientDrawable shape(int color,int stroke) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(10));
        if(stroke!=0)d.setStroke(dp(1),stroke); return d;
    }
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
    private void buildUi(){
        root=column();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(14),dp(8),dp(14),dp(8));top.setBackgroundColor(PANEL);
        TextView logo=label("◈  DevStudio",18,TEXT);logo.setTypeface(null,Typeface.BOLD);top.addView(logo,new LinearLayout.LayoutParams(0,dp(38),1));
        TextView project=label("ANDROID PROJECT  •  main",11,MUTED);top.addView(project);
        Button build=button("▶  BUILD",GREEN);top.addView(build);build.setOnClickListener(v->showBuildInfo());
        root.addView(top);
        LinearLayout body=row();LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1);root.addView(body,bp);
        LinearLayout left=column();left.setBackground(shape(PANEL,BORDER));pad(left,10);
        TextView ft=label("PROJECT",11,MUTED);ft.setTypeface(null,Typeface.BOLD);left.addView(ft);
        tree=column();LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,0,1);tp.topMargin=dp(8);left.addView(tree,tp);renderTree();
        body.addView(left,new LinearLayout.LayoutParams(0,-1,0.22f));
        LinearLayout center=column();center.setPadding(dp(7),dp(0),dp(7),dp(0));
        LinearLayout tabs=row();tabs.setGravity(Gravity.CENTER_VERTICAL);tabs.setBackground(shape(PANEL,BORDER));
        fileTitle=label("  MainActivity.java",12,BLUE);tabs.addView(fileTitle,new LinearLayout.LayoutParams(0,dp(40),1));
        TextView save=label("SAVE  ",11,GREEN);tabs.addView(save);save.setOnClickListener(v->saveEditor());
        center.addView(tabs);
        editor=new EditText(this);editor.setGravity(Gravity.TOP|Gravity.START);editor.setTextColor(TEXT);editor.setHintTextColor(MUTED);
        editor.setTextSize(13);editor.setTypeface(Typeface.MONOSPACE);editor.setPadding(dp(12),dp(12),dp(12),dp(12));
        editor.setBackground(shape(PANEL,BORDER));editor.setSingleLine(false);editor.setHorizontallyScrolling(true);
        editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,0,1);ep.topMargin=dp(7);center.addView(editor,ep);loadFile(activeFile);
        body.addView(center,new LinearLayout.LayoutParams(0,-1,0.48f));
        LinearLayout right=column();right.setBackground(shape(PANEL,BORDER));pad(right,10);
        LinearLayout heading=row();heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView ai=label("✦  AI AGENT",12,TEXT);ai.setTypeface(null,Typeface.BOLD);heading.addView(ai,new LinearLayout.LayoutParams(0,dp(28),1));
        TextView settings=label("API KEY",10,BLUE);heading.addView(settings);settings.setOnClickListener(v->showApiSetup());right.addView(heading);
        LinearLayout modes=row();String[] modeNames={"Think","Write","See file","Create file"};
        for(String m:modeNames){TextView b=label(m,10,m.equals("Think")?BLUE:MUTED);b.setGravity(Gravity.CENTER);b.setPadding(dp(5),dp(7),dp(5),dp(7));b.setBackground(shape(PANEL2,BORDER));
            LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(34),1);mp.setMargins(dp(2),dp(3),dp(2),dp(5));modes.addView(b,mp);
            b.setOnClickListener(v->{prompt.setText(m.equals("See file")?"Read and explain the current file: "+activeFile:m.equals("Create file")?"Create a new file and ask me for its purpose if needed.":m.equals("Think")?"Think through the project and propose a plan.":"Implement the requested changes in the project files.");prompt.setSelection(prompt.length());});
        }right.addView(modes);
        ScrollView scroll=new ScrollView(this);chatMessages=column();scroll.addView(chatMessages);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,0,1);right.addView(scroll,cp);
        addMessage("DevStudio AI","Ready. I can inspect the project context and propose or edit files. Add your API key to begin.",false);
        prompt=new EditText(this);prompt.setTextSize(12);prompt.setTextColor(TEXT);prompt.setHintTextColor(MUTED);prompt.setHint("Ask about your project…");prompt.setMinLines(2);prompt.setMaxLines(4);prompt.setPadding(dp(10),dp(8),dp(10),dp(8));prompt.setBackground(shape(PANEL2,BORDER));
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=dp(6);right.addView(prompt,pp);
        Button send=button("↑  SEND",BLUE);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(38));sp.topMargin=dp(5);right.addView(send,sp);send.setOnClickListener(v->sendPrompt());
        body.addView(right,new LinearLayout.LayoutParams(0,-1,0.30f));
        status=label("  ● Ready     •     Local project workspace",10,MUTED);status.setPadding(dp(8),dp(7),dp(8),dp(7));root.addView(status);
    }
    private Button button(String text,int color){Button b=new Button(this);b.setText(text);b.setTextSize(11);b.setTextColor(BG);b.setAllCaps(false);b.setTypeface(null,Typeface.BOLD);b.setBackground(shape(color,0));return b;}
    private void renderTree(){if(tree==null)return;tree.removeAllViews();
        for(String f:files.keySet()){TextView t=label((f.endsWith(".java")?"  ◈  ":f.endsWith(".gradle")?"  ◇  ":"  ▤  ")+f,11,f.equals(activeFile)?BLUE:TEXT);
            t.setPadding(dp(4),dp(10),dp(2),dp(10));t.setBackground(f.equals(activeFile)?shape(PANEL2,0):shape(PANEL,0));tree.addView(t);
            t.setOnClickListener(v->{saveEditor();activeFile=f;loadFile(f);renderTree();});}
        TextView add=label("+  New file",11,GREEN);add.setPadding(dp(5),dp(12),dp(5),dp(10));tree.addView(add);add.setOnClickListener(v->createFileDialog());
    }
    private void loadFile(String name){fileTitle.setText("  "+name);editor.setText(files.getOrDefault(name,""));}
    private void saveEditor(){if(editor!=null&&activeFile!=null)files.put(activeFile,editor.getText().toString());}
    private void createFileDialog(){EditText e=new EditText(this);e.setSingleLine(true);e.setHint("Example.java");new AlertDialog.Builder(this).setTitle("Create file").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty()||n.contains("..")||n.startsWith("/")||n.contains("/"))return;if(!files.containsKey(n)){saveEditor();files.put(n,"");activeFile=n;renderTree();loadFile(n);}}).show();}
    private void addMessage(String who,String text,boolean user){if(chatMessages==null)return;TextView t=label(who+"\n"+text,12,user?TEXT:MUTED);t.setPadding(dp(9),dp(8),dp(9),dp(8));t.setBackground(shape(user?Color.rgb(36,48,69):PANEL2,BORDER));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(4),0,dp(4));chatMessages.addView(t,p);}
    private void sendPrompt(){
        String question=prompt.getText().toString().trim();if(question.isEmpty())return;
        String key=secrets.getString("api_key","");if(key.isEmpty()){showApiSetup();return;}
        saveEditor();prompt.setText("");addMessage("You",question,true);status.setText("  ● Agent working…");
        JSONArray project=new JSONArray();try{for(Map.Entry<String,String> e:files.entrySet()){JSONObject f=new JSONObject();f.put("path",e.getKey());f.put("content",e.getValue());project.put(f);}}catch(Exception ignored){}
        String instruction="You are DevStudio, an Android coding agent. The user is editing a small project. Return ONLY valid JSON: {\"reply\":\"brief response\",\"actions\":[{\"type\":\"write_file\",\"path\":\"relative filename\",\"content\":\"complete file content\"}]}. Actions are optional. Only use simple relative file names, no slash, no .., no absolute paths. For Think requests do not change files unless explicitly asked. For See file requests explain the current file. Project files JSON: "+project.toString()+"\nUser request: "+question;
        executor.execute(()->{
            String result=callAi(key,instruction);
            runOnUiThread(()->{if(result==null){status.setText("  ● Request failed");addMessage("DevStudio AI","Request failed. Check the API key, network, model access, and API billing.",false);return;}
                try{JSONObject obj=new JSONObject(result);String reply=obj.optString("reply","Done.");JSONArray acts=obj.optJSONArray("actions");
                    if(acts!=null)for(int i=0;i<acts.length();i++){JSONObject a=acts.getJSONObject(i);if("write_file".equals(a.optString("type"))){String path=a.optString("path","");if(!path.isEmpty()&&!path.contains("/")&&!path.contains("..")&&!path.startsWith(".")){files.put(path,a.optString("content",""));activeFile=path;}}}
                    renderTree();loadFile(activeFile);addMessage("DevStudio AI",reply+(acts!=null&&acts.length()>0?"\n\nUpdated "+acts.length()+" file action(s). Tap SAVE to keep editor changes.":"") ,false);status.setText("  ● Ready");
                }catch(Exception ex){addMessage("DevStudio AI",result,false);status.setText("  ● Ready");}
            });
        });
    }
    private String callAi(String key,String promptText){
        HttpURLConnection c=null;try{
            URL url=new URL("https://api.openai.com/v1/chat/completions");c=(HttpURLConnection)url.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setDoOutput(true);
            c.setRequestProperty("Authorization","Bearer "+key);c.setRequestProperty("Content-Type","application/json");
            JSONObject body=new JSONObject();body.put("model","gpt-4o-mini");body.put("temperature",0.2);
            JSONArray msgs=new JSONArray();JSONObject m=new JSONObject();m.put("role","user");m.put("content",promptText);msgs.put(m);body.put("messages",msgs);
            try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}
            InputStream is=(c.getResponseCode()<400)?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=is.read(b))!=-1)out.write(b,0,n);
            JSONObject response=new JSONObject(out.toString("UTF-8"));if(c.getResponseCode()>=400)return null;
            String content=response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
            if(content.startsWith("```"))content=content.replaceFirst("^\\`\\`\\`(?:json)?\\s*","").replaceFirst("\\s*\\`\\`\\`$","");return content;
        }catch(Exception e){return null;}finally{if(c!=null)c.disconnect();}
    }
    private void showApiSetup(){
        LinearLayout box=column();pad(box,8);TextView info=label("OpenAI API key is stored locally on this device. It is sent only to the OpenAI API for your requests. API usage may incur charges.",12,MUTED);box.addView(info);
        EditText key=new EditText(this);key.setSingleLine(true);key.setHint("sk-…");key.setInputType(129);key.setText(secrets.getString("api_key",""));box.addView(key);
        new AlertDialog.Builder(this).setTitle("Connect AI Agent").setMessage("Enter an OpenAI API key").setView(box).setNegativeButton("Later",null).setPositiveButton("Save & test",(d,w)->{
            String k=key.getText().toString().trim();if(k.isEmpty())return;secrets.edit().putString("api_key",k).apply();status.setText("  ● API key saved — testing…");
            executor.execute(()->{String r=callAi(k,"Reply with exactly: DevStudio connection OK");runOnUiThread(()->{status.setText(r==null?"  ● API test failed":"  ● AI connected");addMessage("DevStudio AI",r==null?"Could not verify the key. Check it and your API account.":"API connection test succeeded.",false);});});
        }).show();
    }
    private void showBuildInfo(){new AlertDialog.Builder(this).setTitle("Build APK").setMessage("BUILD runs in GitHub Actions after the project is pushed. This starter repository workflow creates a debug-signed APK and publishes it as an artifact. Save/export and connect repository sync to send this workspace to GitHub.").setPositiveButton("Open workflow",(d,w)->{Intent i=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://github.com/kirovo389-creator/Dev-Studio/actions"));startActivity(i);}).setNegativeButton("Close",null).show();}
    @Override protected void onDestroy(){super.onDestroy();executor.shutdown();}
}
