package cn.huohuas001.huHoBot.Tools;
import net.minecraft.text.Text;
//#if MC<11900
//$$ import net.minecraft.text.LiteralText;
//#endif



public class TextBuilder {
    public static Text build(String text)
    {
        //#if MC>=11900
        return Text.literal(text);
        //#else
        //$$ return new LiteralText(text);
        //#endif
    }
}
