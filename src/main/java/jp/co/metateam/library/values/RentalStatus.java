package jp.co.metateam.library.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RentalStatus implements Values {
    RENT_WAIT(0, "貸出待ち")
    , RENTAlING(1, "貸出中")
    , RETURNED(2, "返却済み")
    , CANCELED(3, "キャンセル");

    private final Integer value;
    private final String text;  


 ////追加しました（5/13） かしまくんimport
        // IntegerからRentalStatusへの変換メソッド
        public static RentalStatus fromInteger(Integer value) {
            if (value == null) {
                return null;
            }
            switch (value) {
                case 0:
                    return RentalStatus.RENT_WAIT;
                case 1:
                    return RentalStatus.RENTAlING;
                case 2:
                    return RentalStatus.RETURNED;
                case 3:
                return RentalStatus.CANCELED;
                default:
                    throw new IllegalArgumentException("Invalid value for RentalStatus: " + value);
            }
        }

    }
    ////ここまでです