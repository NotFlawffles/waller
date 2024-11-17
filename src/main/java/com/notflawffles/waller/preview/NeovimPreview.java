package com.notflawffles.waller.preview;

import com.notflawffles.waller.Waller;

public class NeovimPreview extends Preview {
    public NeovimPreview() {
        super(Waller.class.getClassLoader().getResource("previews/Neovim.png").getFile());
    }
}
