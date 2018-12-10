package ugh.dl.slim;

import lombok.Data;
import ugh.dl.ContentFileArea;

@Data
public class SlimContentFileReference {
    private ContentFileArea area;
    private SlimContentFile file;
}
