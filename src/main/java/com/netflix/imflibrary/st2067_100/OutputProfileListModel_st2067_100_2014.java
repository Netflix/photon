package com.netflix.imflibrary.st2067_100;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.st2067_100.macro.audioRoutingMixing.AudioRoutingMixingMacro;
import com.netflix.imflibrary.st2067_100.macro.audioRoutingMixing.InputEntity;
import com.netflix.imflibrary.st2067_100.macro.audioRoutingMixing.OutputAudioChannel;
import com.netflix.imflibrary.st2067_100.macro.ColorEncoding;
import com.netflix.imflibrary.st2067_100.macro.Macro;
import com.netflix.imflibrary.st2067_100.macro.crop.CropInputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.crop.CropMacro;
import com.netflix.imflibrary.st2067_100.macro.crop.CropOutputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.crop.MXFRectangle;
import com.netflix.imflibrary.st2067_100.macro.crop.RectanglePadding;
import com.netflix.imflibrary.st2067_100.macro.pixelDecoder.PixelDecoderInputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.pixelDecoder.PixelDecoderMacro;
import com.netflix.imflibrary.st2067_100.macro.pixelDecoder.PixelDecoderOutputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.pixelEncoder.PixelEncoderInputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.pixelEncoder.PixelEncoderMacro;
import com.netflix.imflibrary.st2067_100.macro.pixelEncoder.PixelEncoderOutputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.preset.PresetMacro;
import com.netflix.imflibrary.st2067_100.macro.scale.Lanczos;
import com.netflix.imflibrary.st2067_100.macro.scale.ScaleAlgorithm;
import com.netflix.imflibrary.st2067_100.macro.scale.ScaleInputImageSequence;
import com.netflix.imflibrary.st2067_100.macro.scale.ScaleMacro;
import com.netflix.imflibrary.st2067_100.macro.scale.ScaleOutputImageSequence;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.smpte_ra.schemas._2067_100._2014.OutputProfileListType.AliasList.Alias;
import org.smpte_ra.schemas._2067_100._2014.PresetMacroType;
import org.smpte_ra.schemas._2067_101._2014.crop_macro.ImageCropMacroType;
import org.smpte_ra.schemas._2067_101._2014.pixel_decoder.PixelDecoderType;
import org.smpte_ra.schemas._2067_101._2014.pixel_encoder.PixelEncoderType;
import org.smpte_ra.schemas._2067_101._2014.scale_macro.ImageScaleMacroType;
import org.smpte_ra.schemas._2067_103._2014.AudioRoutingMixingMacroType;
import org.smpte_ra.schemas._2067_103._2014.OutputAudioChannelType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class that models OutputProfileList as per specification st2067-100:2014.
 */
@Immutable
class OutputProfileListModel_st2067_100_2014 {
    private final IMFErrorLogger imfErrorLogger;
    private final org.smpte_ra.schemas._2067_100._2014.OutputProfileListType outputProfileListType;
    private final OutputProfileList normalizedOutputProfileList;

    OutputProfileListModel_st2067_100_2014(org.smpte_ra.schemas._2067_100._2014.OutputProfileListType outputProfileListType, IMFErrorLogger imfErrorLogger) {
        this.imfErrorLogger         = imfErrorLogger;
        this.outputProfileListType  = outputProfileListType;
        this.normalizedOutputProfileList = createNormalizedOutputProfileList();
    }

    public OutputProfileList getNormalizedOutputProfileList() {
        return normalizedOutputProfileList;
    }

    /**
     * A stateless method that reads and parses OPL as per st 2067-100:2014 schema and returns normalized(schema agnostic) OutputProfileList object.
     * @return Normalized object model for OutputProfileList
     */
    private OutputProfileList createNormalizedOutputProfileList () {
        Map<String, Macro> macroMap = new HashMap<>();
        Map<String, String> aliasMap = new HashMap<>();
        for(org.smpte_ra.schemas._2067_100._2014.MacroType macroType: outputProfileListType.getMacroList().getMacro()) {

            Macro macro = createMacro(macroType);
            if(macro != null) {
                macroMap.put(macroType.getName(), macro);
            }
        }

        for(Alias alias: outputProfileListType.getAliasList().getAlias()) {
            aliasMap.put(alias.getValue(), alias.getHandle());
        }

        OutputProfileList normalizedOutputProfileList = new OutputProfileList( outputProfileListType.getId(),
                outputProfileListType.getAnnotation() != null ? outputProfileListType.getAnnotation().getValue() : null,
                outputProfileListType.getCompositionPlaylistId(), aliasMap, macroMap);

        this.imfErrorLogger.addAllErrors(normalizedOutputProfileList.getErrors());

        return normalizedOutputProfileList;
    }

    private Macro createMacro(org.smpte_ra.schemas._2067_100._2014.MacroType macroType) {
        if(macroType instanceof ImageCropMacroType) {
            return createCropMacro((ImageCropMacroType) macroType);
        }
        else if(macroType instanceof ImageScaleMacroType) {
            return createScaleMacro((ImageScaleMacroType) macroType);
        }
        else if(macroType instanceof PixelDecoderType) {
            return createPixelDecoderMacro((PixelDecoderType) macroType);
        }
        else if(macroType instanceof PixelEncoderType) {
            return createPixelEncoderMacro((PixelEncoderType) macroType);
        }
        else if(macroType instanceof AudioRoutingMixingMacroType) {
            return createAudioRoutingMixingMacro((AudioRoutingMixingMacroType) macroType);
        }
        else if(macroType instanceof PresetMacroType) {
            return createPresetMacro((PresetMacroType) macroType);
        }
        else {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Unknown macro type with %s in OPL", macroType.getName()));
            return null;
        }
    }


    private CropMacro createCropMacro(ImageCropMacroType imageCropMacroType) {
        if(imageCropMacroType.getInputImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing InputImageSequence in %s macro", imageCropMacroType.getName()));
        }
        ImageCropMacroType.InputImageSequence inputImageSequence = imageCropMacroType.getInputImageSequence();

        MXFRectangle mxfRectangleEnum = MXFRectangle.fromValue(inputImageSequence.getReferenceRectangle().value());
        RectanglePadding inset = new RectanglePadding(inputImageSequence.getInset().getLeft().intValue(),
                inputImageSequence.getInset().getRight().intValue(),
                inputImageSequence.getInset().getTop().intValue(),
                inputImageSequence.getInset().getBottom().intValue());

        CropInputImageSequence input = new CropInputImageSequence(inputImageSequence.getAnnotation() != null ? inputImageSequence.getAnnotation().getValue() : null, 
                inputImageSequence.getHandle(), mxfRectangleEnum, inset);

        if(imageCropMacroType.getOutputImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing OutputImageSequence in %s macro", imageCropMacroType.getName()));
        }
        ImageCropMacroType.OutputImageSequence outputImageSequence = imageCropMacroType.getOutputImageSequence();
        RectanglePadding padding = new RectanglePadding(outputImageSequence.getPadding().getLeft().intValue(),
                outputImageSequence.getPadding().getRight().intValue(),
                outputImageSequence.getPadding().getTop().intValue(),
                outputImageSequence.getPadding().getBottom().intValue());


        ColorEncoding colorEncodingEnum = ColorEncoding.fromValue(outputImageSequence.getFillColor().getClass().getName());
        CropOutputImageSequence output = new CropOutputImageSequence( outputImageSequence.getAnnotation() != null ? outputImageSequence.getAnnotation().getValue() : null,
                "macros/" + imageCropMacroType.getName() + "/outputs/images",
                padding, colorEncodingEnum);

        return new CropMacro( imageCropMacroType.getName(),
                imageCropMacroType.getAnnotation() != null ? imageCropMacroType.getAnnotation().getValue() : null,
                input, output);
    }

    private ScaleMacro createScaleMacro(ImageScaleMacroType imageScaleMacroType) {
        if(imageScaleMacroType.getInputImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing InputImageSequence in %s macro", imageScaleMacroType.getName()));
        }
        ImageScaleMacroType.InputImageSequence inputImageSequence = imageScaleMacroType.getInputImageSequence();
        ScaleInputImageSequence input = new ScaleInputImageSequence(inputImageSequence.getAnnotation() != null ? inputImageSequence.getAnnotation().getValue() : null, 
                inputImageSequence.getHandle());

        if(imageScaleMacroType.getOutputImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing OutputImageSequence in %s macro", imageScaleMacroType.getName()));
        }
        ImageScaleMacroType.OutputImageSequence outputImageSequence = imageScaleMacroType.getOutputImageSequence();
        ScaleAlgorithm scaleAlgorithmType = null;
        if(outputImageSequence.getAlgorithm() instanceof org.smpte_ra.schemas._2067_101._2014.lanczos.LanczosType) {
            scaleAlgorithmType = new Lanczos(((org.smpte_ra.schemas._2067_101._2014.lanczos.LanczosType) outputImageSequence.getAlgorithm()).getParameterA().intValue());
        }
        if(scaleAlgorithmType == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Unsupported scaling algorithm in %s macro", imageScaleMacroType.getName()));
        }
        ScaleOutputImageSequence output = new ScaleOutputImageSequence( outputImageSequence.getAnnotation() != null ? outputImageSequence.getAnnotation().getValue() : null,
                "macros/" + imageScaleMacroType.getName() + "/outputs/images",
                outputImageSequence.getWidth().intValue(), outputImageSequence.getHeight().intValue(), outputImageSequence.getBoundaryCondition(), scaleAlgorithmType);

        return new ScaleMacro( imageScaleMacroType.getName(),
                imageScaleMacroType.getAnnotation() != null ? imageScaleMacroType.getAnnotation().getValue() : null,
                input, output);
    }


    private PixelDecoderMacro createPixelDecoderMacro(PixelDecoderType pixelDecoderType) {
        if(pixelDecoderType.getInputImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing InputImageSequence in %s macro", pixelDecoderType.getName()));
        }
        PixelDecoderType.InputImageSequence inputImageSequence = pixelDecoderType.getInputImageSequence();
        PixelDecoderInputImageSequence input = new PixelDecoderInputImageSequence(inputImageSequence.getAnnotation() != null ? inputImageSequence.getAnnotation().getValue() : null, 
                inputImageSequence.getHandle());

        if(pixelDecoderType.getOutputReferenceImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing OutputImageSequence in %s macro", pixelDecoderType.getName()));
        }
        PixelDecoderType.OutputReferenceImageSequence outputImageSequence = pixelDecoderType.getOutputReferenceImageSequence();
        PixelDecoderOutputImageSequence output = new PixelDecoderOutputImageSequence( outputImageSequence.getAnnotation() != null ? outputImageSequence.getAnnotation().getValue() : null,
                "macros/" + pixelDecoderType.getName() + "/outputs/images");

        return new PixelDecoderMacro(pixelDecoderType.getName(),
                pixelDecoderType.getAnnotation() != null ? pixelDecoderType.getAnnotation().getValue() : null,
                input, output);
    }

    private PixelEncoderMacro createPixelEncoderMacro(PixelEncoderType pixelEncoderType) {
        if(pixelEncoderType.getInputReferenceImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing InputImageSequence in %s macro", pixelEncoderType.getName()));
        }
        PixelEncoderType.InputReferenceImageSequence inputImageSequence = pixelEncoderType.getInputReferenceImageSequence();
        PixelEncoderInputImageSequence input = new PixelEncoderInputImageSequence(inputImageSequence.getAnnotation() != null ? inputImageSequence.getAnnotation().getValue() : null, 
                inputImageSequence.getHandle());

        if(pixelEncoderType.getOutputImageSequence() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing OutputImageSequence in %s macro", pixelEncoderType.getName()));
        }
        PixelEncoderType.OutputImageSequence outputImageSequence = pixelEncoderType.getOutputImageSequence();
        PixelEncoderOutputImageSequence output = new PixelEncoderOutputImageSequence( outputImageSequence.getAnnotation() != null ? outputImageSequence.getAnnotation().getValue() : null,
                "macros/" + pixelEncoderType.getName() + "/outputs/images");

        return new PixelEncoderMacro(pixelEncoderType.getName(),
                pixelEncoderType.getAnnotation() != null ? pixelEncoderType.getAnnotation().getValue() : null,
                input, output);
    }

    private AudioRoutingMixingMacro createAudioRoutingMixingMacro(AudioRoutingMixingMacroType audioRoutingMixingMacroType) {
        if(audioRoutingMixingMacroType.getOutputEntityList() == null) {
            this.imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_OPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("Missing OutputEntityList in %s macro", audioRoutingMixingMacroType.getName()));
        }

        AudioRoutingMixingMacroType.OutputEntityList outputEntityList = audioRoutingMixingMacroType.getOutputEntityList();
        List<OutputAudioChannel> outputAudioChannels = new ArrayList<>();
        for(OutputAudioChannelType outputAudioChannelType: outputEntityList.getOutputAudioChannel()) {
            List<InputEntity> inputEntityList = outputAudioChannelType.getInputEntityList().getInputEntity().stream().map(e -> new InputEntity( "", e.getHandle(), e.getGain())).collect(Collectors.toList());
            outputAudioChannels.add(new OutputAudioChannel( outputAudioChannelType.getAnnotation() != null ? outputAudioChannelType.getAnnotation().getValue() : null,
                    "macros/" + audioRoutingMixingMacroType.getName() + "/outputs/" + outputAudioChannelType.getHandle(), inputEntityList));
        }

        return new AudioRoutingMixingMacro(audioRoutingMixingMacroType.getName(),
                audioRoutingMixingMacroType.getAnnotation() != null ? audioRoutingMixingMacroType.getAnnotation().getValue() : null,
                outputAudioChannels);
    }

    private PresetMacro createPresetMacro(PresetMacroType presetMacroType) {
        return new PresetMacro(presetMacroType.getName(),
                presetMacroType.getAnnotation() != null ? presetMacroType.getAnnotation().getValue() : null,
                presetMacroType.getPreset());
    }
}