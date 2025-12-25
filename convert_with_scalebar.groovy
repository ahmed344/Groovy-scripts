/*
 * ImageJ/Fiji Script: DM3 to TIFF Converter with Scale Bar
 * 
 * This script processes DM3 (Digital Micrograph) images by adding a scale bar
 * and converting them to TIFF format.
 * 
 * Features:
 * - Batch processes all .dm3 files in a selected directory
 * - Adds a 1 micrometer scale bar to each image
 * - Saves converted images as TIFF files in an output directory
 * - Properly closes all images to prevent memory issues
 */

// Prompt user to select the directory containing DM3 files
dir = getDirectory("Choose a folder with DM3 files");

// Get a list of all files in the selected directory
list = getFileList(dir);

// Define the output directory path
outputDir = dir + "converted_with_scalebar/";

// Create the output directory if it doesn't exist
File.makeDirectory(outputDir);

// Loop through all files in the directory
for (i = 0; i < list.length; i++) {
    // Process only files with .dm3 extension
    if (endsWith(list[i], ".dm3")) {
    	
    	// Open the image
        open(dir + list[i]);
        
        // Store the original image title to close it after
        originalTitle = getTitle();

        // Add scalebar as an overlay to the image
        run("Scale Bar...", "width=1 height=20 thickness=20 font=100 color=Black background=None location=[Lower Right] bold overlay");

        // Flatten the overlay onto the image
        run("Flatten");
        
        // Save as TIF
        saveAs("Tiff", outputDir + File.nameWithoutExtension + "_scaled.tif");
        
        // Close flattened image
        close();
        
        // Close original image window using the title stored earlier
        selectWindow(originalTitle);
        close();
    }
}
