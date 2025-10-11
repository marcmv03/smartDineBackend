package com.smartDine.dto ;

import com.smartDine.entity.DrinkType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrinkDTO extends MenuItemDTO {
    private DrinkType drinkType ;
    public DrinkDTO() {
        super() ;
    }

}