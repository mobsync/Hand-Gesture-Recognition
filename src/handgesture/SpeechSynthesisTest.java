package handgesture;

import com.sun.speech.freetts.*;

public class SpeechSynthesisTest
{
    public static void main(String[] args)
    {
        Voice voiceKevin16 = new Voice("kevin16");

        String[] thingsToSay = new String[]
        {
            "hi everybody",
            "my name is Vilas Sangre",
            "my voice is built into free t t s",
            "but it isn't very mellifluous",
            "it could be worse, though",
            "every time my friend alan tries to talk",
            "about anything more exciting than what time it is",
            "he barfs up a bunch of exceptions",
            "and passes out",
        };

        voiceKevin16.say(thingsToSay);
    }
    
    public static void getVoice(String data)
    {

        Voice voiceKevin16 = new Voice("kevin16");

        String[] thingsToSay = new String[]
        {
           data
        };

        voiceKevin16.say(thingsToSay);
    }
    
    
}

class Voice
{
    private String name;
    private com.sun.speech.freetts.Voice systemVoice;

    public Voice(String name)
    {
        this.name = name;
        this.systemVoice = VoiceManager.getInstance().getVoice(this.name);
        this.systemVoice.allocate();
    }

    public void say(String[] thingsToSay)
    {
        for (int i = 0; i < thingsToSay.length; i++)
        {
            this.say( thingsToSay[i] );
        }
    }

    public void say(String thingToSay)
    {
        this.systemVoice.speak(thingToSay);
    }

    public void dispose()
    {
        this.systemVoice.deallocate();
    }
}