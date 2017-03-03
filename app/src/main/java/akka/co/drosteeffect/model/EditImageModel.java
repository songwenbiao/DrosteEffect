package akka.co.drosteeffect.model;

import android.graphics.Rect;

import java.util.ArrayList;

import akka.co.drosteeffect.contract.EditImageContract;

/**
 * Created by swb on 2017/3/2.
 */

public class EditImageModel implements EditImageContract.Model {
    @Override
    public void calculateFrame(ArrayList<Rect> pictureframes, ArrayList<Rect> innerframe, int piece) {

        int finalinnerwidth = pictureframes.get(0).width();
        int originalwidth = innerframe.get(0).width();
        int finalinnerHeight = pictureframes.get(0).height();
        int originalHeight = innerframe.get(0).height();


        int innercenterx = originalwidth / 2 + innerframe.get(0).left; //内部矩形，中心点x坐标
        int innercentery = originalHeight / 2 + innerframe.get(0).top; //内部矩形， 中心点y坐标

        int pictureframecenterx = finalinnerwidth / 2 + pictureframes.get(0).left; //大矩形， 中心点x坐标
        int pictureframecentery = finalinnerHeight / 2 + pictureframes.get(0).top; //大矩形， 中心点y坐标


        int offsetX = Math.abs(pictureframecenterx - innercenterx); //大小矩形中心的x轴间距
        int offsety = Math.abs(pictureframecentery - innercentery); //大小矩形中心的y轴间距

        for (int i = 1; i <= piece; i++) {
            double routeRate = Math.pow((double) i / piece, 2.0);

            int width = originalwidth + (int) ((finalinnerwidth - originalwidth) * routeRate);
            int height = originalHeight + (int) ((finalinnerHeight - originalHeight) * routeRate);

            int temcenterX, temcenterY;

            if (innercenterx > pictureframecenterx) {
                temcenterX = (int) (innercenterx - ((float) offsetX * routeRate));
            } else {
                temcenterX = (int) (innercenterx + ((float) offsetX * routeRate));
            }
            if (innercentery > pictureframecentery) {
                temcenterY = (int) (innercentery - ((float) offsety * routeRate));
            } else {
                temcenterY = (int) (innercentery + ((float) offsety * routeRate));
            }

            int tmpInnerLeft = temcenterX - width / 2;
            int tmpInnerTop = temcenterY - height / 2;


            Rect rectInnerframe = new Rect(tmpInnerLeft, tmpInnerTop, tmpInnerLeft + width, tmpInnerTop
                    + height);
            innerframe.add(i, rectInnerframe);

            float tmprate = (float) (width) / originalwidth;
            int leftOfOuterPicture = innerframe.get(0).left - (int) (tmpInnerLeft / tmprate);
            int topOfOuterPicture = innerframe.get(0).top - (int) (tmpInnerTop / tmprate);
            int outerwidth = (int) (finalinnerwidth / tmprate);
            int outerheight = (int) (finalinnerwidth / tmprate);

            Rect rectPictureframes = new Rect(leftOfOuterPicture, topOfOuterPicture, leftOfOuterPicture +
                    outerwidth, topOfOuterPicture + outerheight);
            pictureframes.add(i, rectPictureframes);

        }
    }
}
