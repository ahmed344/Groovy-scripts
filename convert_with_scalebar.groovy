/*
 * ==================================================================================
 * ImageJ/Fiji Script: DM3 to TIFF Converter with Scale Bar
 * ==================================================================================
 * 
 * This script processes DM3 (Digital Micrograph) images by adding a scale bar
 * and converting them to TIFF format.
 * 
 * Features:
 * - Batch processes all .dm3 files in a selected directory
 * - Enhances image contrast for better visibility
 * - Adds a customizable scale bar to each image
 * - Saves converted images as TIFF files in an output directory
 * - Properly closes all images to prevent memory issues
 * 
 * ==================================================================================
 */

// ==================================================================================
// CONFIGURATION PARAMETERS
// Modify these values according to your needs
// ==================================================================================

// OUTPUT DIRECTORY CONFIGURATION
// --------------------------------------------------------------------------------
// Name of the output subdirectory where converted images will be saved
// Examples: "converted_with_scalebar/", "output/", "processed_images/"
outputDirName = "converted_with_scalebar/";

// OUTPUT FILENAME SUFFIX
// --------------------------------------------------------------------------------
// Suffix added to the output filename (before the .tif extension)
// Examples: "_scaled", "_processed", "_sb", ""
filenameSuffix = "_scaled";

// CONTRAST ENHANCEMENT
// --------------------------------------------------------------------------------
// Saturation level for contrast enhancement (percentage of pixels to clip)
// Range: 0.0 to 5.0 (typical: 0.1 to 1.0)
// Examples: 0.1 (minimal), 0.35 (moderate), 1.0 (strong)
contrastSaturation = 0.35;

// SCALE BAR PARAMETERS
// --------------------------------------------------------------------------------
// Physical length of the scale bar in micrometers (um)
// Examples: 0.5, 1, 2, 5, 10
scaleBarWidth = 1;

// Maximum fraction of the image width the scale bar should occupy.
// The script picks the largest clean scale bar length that fits this limit.
// Examples: 0.20, 0.25, 0.33
scaleBarMaxImageFraction = 0.25;

// Height of the scale bar text label (in pixels)
// Examples: 10, 15, 20, 25
scaleBarHeight = 20;

// Thickness of the scale bar line (in pixels)
// Examples: 10, 15, 20, 25, 30
scaleBarThickness = 20;

// Font size for the scale bar label text
// Examples: 50, 70, 100, 120, 150
scaleBarFont = 100;

// Color of the scale bar and text
// Options: "White", "Black", "Red", "Green", "Blue", "Yellow"
scaleBarColor = "Black";

// Background behind the scale bar
// Options: "None" (transparent), "White", "Black", etc.
scaleBarBackground = "None";

// Location of the scale bar on the image
// Options: "[Lower Right]", "[Lower Left]", "[Upper Right]", "[Upper Left]"
scaleBarLocation = "[Lower Right]";

// ==================================================================================
// END OF CONFIGURATION PARAMETERS
// ==================================================================================

// Convert the configured micrometer scale bar width to the image calibration unit.
function convertMicrometersToImageUnit(widthMicrometers, imageUnit) {
    unit = toLowerCase(imageUnit);

    if (unit == "um" || unit == "µm" || unit == "micron" || unit == "microns" || unit == "micrometer" || unit == "micrometers" || unit == "micrometre" || unit == "micrometres")
        return widthMicrometers;

    if (unit == "nm" || unit == "nanometer" || unit == "nanometers" || unit == "nanometre" || unit == "nanometres")
        return widthMicrometers * 1000;

    if (unit == "mm" || unit == "millimeter" || unit == "millimeters" || unit == "millimetre" || unit == "millimetres")
        return widthMicrometers / 1000;

    if (unit == "cm" || unit == "centimeter" || unit == "centimeters" || unit == "centimetre" || unit == "centimetres")
        return widthMicrometers / 10000;

    exit("Unsupported image calibration unit '" + imageUnit + "' for scale bar conversion.");
}

// Pick the largest clean 1/2/5 scale bar width that fits the target width.
function getNiceScaleBarWidth(maxWidth) {
    if (maxWidth <= 0)
        exit("Scale bar width must be greater than zero.");

    niceWidth = 1;

    while (niceWidth > maxWidth)
        niceWidth = niceWidth / 10;

    while (niceWidth * 10 <= maxWidth)
        niceWidth = niceWidth * 10;

    if (niceWidth * 5 <= maxWidth)
        return niceWidth * 5;

    if (niceWidth * 2 <= maxWidth)
        return niceWidth * 2;

    return niceWidth;
}

// Use the configured width as an upper limit, but shrink it for high magnification images.
function getAutomaticScaleBarWidth(configuredWidth, imageWidthPixels, pixelWidth, maxImageFraction) {
    maxVisibleWidth = imageWidthPixels * pixelWidth * maxImageFraction;

    if (configuredWidth < maxVisibleWidth)
        targetWidth = configuredWidth;
    else
        targetWidth = maxVisibleWidth;

    return getNiceScaleBarWidth(targetWidth);
}


// ==================================================================================
// MAIN PROCESSING SCRIPT
// Do not modify below unless you know what you're doing
// ==================================================================================

// Prompt user to select the directory containing DM3 files
sourceDir = getDirectory("Choose a folder with DM3 files");

// Define the full output directory path (source directory + output subdirectory name)
outputDir = sourceDir + outputDirName;

// Create the output directory if it doesn't already exist
File.makeDirectory(outputDir);

// Get a list of all files in the selected source directory
list = getFileList(sourceDir);

// Loop through all files in the directory
for (i = 0; i < list.length; i++) {
    
    // Process only files with .dm3 extension (skip other file types)
    if (endsWith(list[i], ".dm3")) {
    	
    	// Open the DM3 image file
        open(sourceDir + list[i]);
        
        // Store the original image title for later reference
        originalTitle = getTitle();

        // Convert the configured micrometer width into the image's current unit
        getPixelSize(imageUnit, pixelWidth, pixelHeight, voxelDepth);
        convertedScaleBarWidth = convertMicrometersToImageUnit(scaleBarWidth, imageUnit);
        automaticScaleBarWidth = getAutomaticScaleBarWidth(convertedScaleBarWidth, getWidth(), pixelWidth, scaleBarMaxImageFraction);
        print(list[i] + ": scale bar " + scaleBarWidth + " um requested, using " + automaticScaleBarWidth + " " + imageUnit);

        // Enhance contrast (stretches the histogram to improve visibility)
        run("Enhance Contrast", "saturated=" + contrastSaturation);

        // Add scale bar as an overlay to the image
        run("Scale Bar...", 
            "width=" + automaticScaleBarWidth + 
            " height=" + scaleBarHeight + 
            " thickness=" + scaleBarThickness + 
            " font=" + scaleBarFont + 
            " color=" + scaleBarColor + 
            " background=" + scaleBarBackground + 
            " location=" + scaleBarLocation + 
            " bold overlay");

        // Flatten the overlay onto the image
        run("Flatten");
        
        // Save the flattened image as a TIFF file
        saveAs("Tiff", outputDir + File.nameWithoutExtension + filenameSuffix + ".tif");
        
        // Close the flattened image
        close();
        
        // Select the original image window using the title stored earlier
        selectWindow(originalTitle);
        close();
    }
}
