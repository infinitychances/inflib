package com.infinitychances.inflib.model;

import static com.infinitychances.inflib.InfLib.LOGGER;
import com.infinitychances.inflib.exceptions.InvalidInputException;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class ExtModel{
    private static HashMap<String, ExtModel> idMap = new HashMap<>();
    private static ArrayList<String> usedIds = new ArrayList<>();

    public Model model;
    public String path;
    public ExtModelType type;
    public String id;
    public String modOrigin;
    public TextureKey[] requiredKeys;

    private ExtModel() {
        this.model = null;
        this.path = null;
        this.type = null;
        this.id = null;
        this.modOrigin = null;
        this.requiredKeys = null;
    }

    //Base constructor that gets referenced using of
    private ExtModel(String modOrigin, ExtModelType type, String name, Optional<String> variant, TextureKey... textures) {
        if(name.contains("/")) {
            LOGGER.error("Invalid Parent Model: ");
            throw new IllegalArgumentException("Parent Model cannot contain a /!");
        }
        switch (type) {
            case BLOCK_VARIANT:
                if(variant.isEmpty()) {
                    throw new IllegalArgumentException("Cannot use Block Variant without providing a valid variant!");
                }
                this.model = block(modOrigin, name, variant, textures);
                this.path = "block/" + name + variant.orElse("");
                break;
            case BLOCK:
                this.model = block(modOrigin, name, textures);
                this.path = "block/" + name;
                break;
            case ITEM:
                this.model = item(modOrigin, name, textures);
                this.path = "item/" + name;
                break;
            case NORMAL:
                this.model = make(textures);
                this.path = name;
                break;
            default:
                LOGGER.error("Invalid Type");
        }
        if (type == ExtModelType.BLOCK_VARIANT) {
            this.model = block(modOrigin, name, variant, textures);
            this.path = "block/" + name;
        } else {
            LOGGER.error("Variant Not Supported With this type");
        }
        String tempId = getID(type, name);
        if (!checkId(tempId)){
            throw new InvalidInputException("DUPLICATE MODEL BEING CREATED", type + "§" + name);
        }
        this.id = tempId;
        idMap.put(this.id, this);

        this.requiredKeys = textures;
        this.modOrigin = modOrigin;
        this.type = type;
    }

    public static ExtModel of(String modOrigin, ExtModelType type, String name, String variant, TextureKey... textures) {
        return new ExtModel(modOrigin,type, name, Optional.of(variant),textures);
    }

    public static ExtModel of(String modOrigin, ExtModelType type, String name, TextureKey... textures) {
        return new ExtModel(modOrigin,type, name,Optional.empty(),textures);
    }

    public static ExtModel of(ExtModelType type, String name, String variant, TextureKey... textures) {
        return new ExtModel("minecraft",type, name,Optional.of(variant),textures);
    }

    public static ExtModel of(ExtModelType type, String name, TextureKey... textures) {
        return new ExtModel("minecraft",type,name,Optional.empty(),textures);
    }

    private static Model make(TextureKey... requiredTextureKeys) {
        return new Model(Optional.empty(), Optional.empty(), requiredTextureKeys);
    }

    private static Model block(String modOrigin, String name, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(Identifier.of(modOrigin, "block/" + name)), Optional.empty(), requiredTextureKeys);
    }

    private static Model item(String modOrigin, String name, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(Identifier.of(modOrigin, "item/" + name)), Optional.empty(), requiredTextureKeys);
    }

    private static Model block(String modOrigin, String name, Optional<String> variant, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(Identifier.of(modOrigin, "block/" + name)), variant, requiredTextureKeys);
    }

    //returns the ExtModel of the id
    //This is probably a useless function
    public static ExtModel getExtModelFromId(String id) {
        return idMap.get(id);
    }

    //Returns the section of the path variable that states the name of the model json file.
    public String parsePath(){
        return parsePath(this.path);
    }

    public static String parsePath(String path){
        return (path.split("/"))[1];
    }

    public HashMap<ExtModelType, String> parseId() {
        return parseId(this.id);
    }

    public static HashMap<ExtModelType, String> parseId(String id) {
        String[] idArray = decodeId(id).split("§");
        HashMap<ExtModelType, String> map = new HashMap<>();
        map.put(ExtModelType.valueOf(idArray[0]), idArray[1]);
        return map;
    }

    //Gives the id from the name.
    private static String getID(@NotNull ExtModelType type, String name) {
        String str = type.name() +"§"+ name;
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    //checks if an id is a duplicate
    private static boolean checkId(String id) {
        if(usedIds.contains(id)) {
            LOGGER.error("DUPLICATE MODEL FOUND");
            return false;
        }
        usedIds.add(id);
        return true;
    }

    protected static String decodeId(String id) {
        return Arrays.toString(Base64.getDecoder().decode(id));
    }

    public void createBlockModel(Block block, TextureMap textures, BlockStateModelGenerator blockStateModelGenerator, String modId) {
        ExtModels.createBlockModel(this, block, textures, blockStateModelGenerator, modId);
    }

    public void createBlockModel(Block block, TextureMap textures, BlockStateModelGenerator blockStateModelGenerator) {
        ExtModels.createBlockModel(this, block, textures, blockStateModelGenerator);
    }

    /*public static ExtModelBuilder create() {
        return ExtModelBuilder.create();
    }*/
}


