package com.notflawffles.waller.preview;

import com.notflawffles.waller.Waller;

public class TerminalPreview extends Preview {
    public TerminalPreview() {
        super(Waller.class.getClassLoader().getResource("previews/Terminal.png").getFile());
    }
}
