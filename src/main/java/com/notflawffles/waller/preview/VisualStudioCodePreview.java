package com.notflawffles.waller.preview;

import com.notflawffles.waller.Waller;

public class VisualStudioCodePreview extends Preview {
    public VisualStudioCodePreview() {
        super(Waller.class.getClassLoader().getResource("previews/VisualStudioCode.png").getFile());
    }
}
