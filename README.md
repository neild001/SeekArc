SeekArc
=======

### What is a SeekArc?

So what the heck is a SeekArc? Essentially it’s a SeekBar that has been wrapped around a circle. It acts like a SeekBar and generally has the same settings. You can even add a Listener to it as you would with a SeekBar. So if its in a circle why not call it a SeekCircle? The answer is that the SeekArc does have a few more setting than the SeekBar one of these is the sweepAngle, which means that the SeekArc doesn’t have to be drawn as a full circle, and can be drawn as a circle, semi-circle  or quarter arc or whatever you like.

<div align="center">
  <img height="200px" src="https://raw.github.com/neild001/SeekArc/master/raw/arc_sweeps.png"/>
</div>

### Sample Project

You can see the SeekArc working in the sample application. Just check out the library and sample application as two separate projects and import them into eclipse. Make sure that the Sample app is setup to correctly use the library project. The sample app will let you explore the attributes that can be set on the SeekArc

<div align="center">
  <img height="400px" src="https://raw.github.com/neild001/SeekArc/master/raw/sample_app.png"/>
</div>

### Using the SeekArc

Using the SeekArc is straightforward. There are a number of settings from sweep angle to rotation that can be set. One of the less obvious settings is the radius or diameter of the SeekArc. When I developed the SeekArc I thought about adding an attribute for defining the diameter of the circle. In the end I decided against this. I thought a more flexible approach would be to allow the SeekArc to expand to the size of  its container layout. This way the SeekArc can grow in size with a layout that has widths or heights of match parent. Also it is still possible to set the SeekArc to a specific size by setting the container layouts width and height to specific dp values. This sizing approach gives the best of both worlds. To further adjust how the arc fits in its container a padding attribute can also be used.

To help with understanding how to use the SeeekArc I’ve put together a sample app with a number of controls that can be used to adjust the attributes of the SeekArc. This is by far the best way to get a good understanding of how to use the SeekArc. From the sample app you’ll see that it is possible to set attributes such as:

Sweep angle,
Rotation,
Clockwise, (which way do you want the progress to increase clockwise/anticlockwise),
Arc and progress width,
Rounded corners and 
Touch inside enable/disable.


### To use it in your code

Simply add the View to your Layout

        <com.triggertrap.seekarc.SeekArc
            android:id="@+id/seekArc"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:padding="30dp"
            seekarc:rotation="180"
            seekarc:startAngle="30"
            seekarc:sweepAngle="300"
            seekarc:touchInside="true" />



