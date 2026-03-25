package automation.core;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import automation.core.Log;

import java.io.File;

/**
 * PDF processing helper using Apache PDFBox.
 */
public class PdfHelper
{

    public static String extractText(String filePath)
    {
        try
        {
            PDDocument document = Loader.loadPDF(new File(filePath));
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        }
        catch (Exception e)
        {
            Log.error("PDF text extraction failed: " + e.getMessage());
            return "";
        }
    }

    public static String extractText(String filePath, int startPage, int endPage)
    {
        try
        {
            PDDocument document = Loader.loadPDF(new File(filePath));
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            String text = stripper.getText(document);
            document.close();
            return text;
        }
        catch (Exception e)
        {
            Log.error("PDF text extraction failed: " + e.getMessage());
            return "";
        }
    }

    public static int getPageCount(String filePath)
    {
        try
        {
            PDDocument document = Loader.loadPDF(new File(filePath));
            int pages = document.getNumberOfPages();
            document.close();
            return pages;
        }
        catch (Exception e)
        {
            Log.error("PDF page count failed: " + e.getMessage());
            return 0;
        }
    }

    public static boolean containsText(String filePath, String searchText)
    {
        String content = extractText(filePath);
        return content.contains(searchText);
    }
}
