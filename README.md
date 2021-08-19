# CovCT: An Android Application for COVID-19 & Non-COVID-19 Detection/Classification using Lung Computed Tomography Images

[![GitHub stars](https://img.shields.io/github/stars/monjoybme/CovCT_application)](https://github.com/monjoybme/CovCT_application/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/monjoybme/CovCT_application)](https://github.com/monjoybme/CovCT_application/issues)
[![GitHub forks](https://img.shields.io/github/forks/monjoybme/CovCT_application)](https://github.com/monjoybme/CovCT_application/network)
![GitHub License](https://img.shields.io/github/license/monjoybme/CovCT_application)



Chest CT scans has a potential role in the diagnosis and prognostication of COVID-19 and the designing of a portable and cost-effective diagnosis system capable of running on resource constrained devices like Android OS Phones would indeed improve the clinical utility of chest CT.

<b>CovCT</b> is an android application developed to detect COVID-19 (CoronaVirus Disease 2019) from Chest CT Scan images. The application uses the power of 
Deep Neural Networks to detect COVID-19 from Chest CT Scans and further generate a heatmap which shows the affected regions in the chest. 
<h3>Features of CovCT app:</h3>
<ol>
<li>Segmentation of Lung region from the CT scans through a robust algorithm of Computer Vision.
<li>Predicting the Covid-19 percentage in the Lungs from the CT Scan.
<li>Generating the heatmaps indicating the regions of Covid-19 infection in the lungs.
<li>Augmenting the heatmaps with Segmented lungs and saving. 
</ol>
<h3>Results from CovCT Application:</h3>
<p align="center">
<img src="Demostration%20and%20Results/Image%20Results/Original%20Images/137covid_patient10_SR_3_IM00011.png" width="200" height="200" >   <img src="Demostration and Results/Image Results/Augmented Results/1628400535190.jpg" width="200"> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/Image Results/Original Images/137covid_patient7_SR_3_IM00022.png" width="200" height="200" >   <img src="Demostration and Results/Image Results/Augmented Results/1628400864223.jpg" width="200"></p>
<h2>Installation :</h2>
<ul>
<li>The app can be installed by directly <b>downloading the Android Application</b> Package (APK) File located at : "Demonstration and results/app-debug.apk" into the <b>Mobile phone device with Android OS</b>.
<li>For <b>building the project on your local machine</b>, clone the repo and Open it with Android Studio.
</ul>
</br>
<h2>Usage of CovCT App:</h2>
<ol>
<li>After installing or building the app, the first screen which is encountered is a <b>Splash screen with animation and CovCT Logo</b>. Before this step remember to download the sample images from the "Demonstration and Results/Image Results" folder for testing purposes. The second screen comes for <b>asking permission from user</b> of :<ul>
  <li>WRITE_EXTERNAL_STORAGE
  <li>READ_EXTERNAL_STORAGE</ul>
  Which are needed for getting the images from gallery and storing processed heatmaps into mobile phone storage.
  <p align=center><img src="Demostration and Results/ScreenShots/1.jpg" width="200" height="350" >&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/2.jpg" width="200" height="350" > &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/3.jpg" width="200" height="350" >  </p>
<li>The third screen Renders <b>Home Page</b> of the application which shows option of Scrolling to the next fragment which is CT Scan loading Fragment.
  This Activity also contain <b>Splits</b> navigating to the <b>extra options</b> for sharing app, developer info and link to this github repo.
  <p align=center><img src="Demostration and Results/ScreenShots/4.jpg" width="200" height="350" > &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/5.jpg" width="200" height="350" >  </p>
  <li>The fifth screen gives us the option to <b>Load our CT Image from Phone Storage</b>. This acitivity checks for the size of image and type of image and throws error if images are of unequal dimensions. Following the Uploading of the image comes the further steps in the same activity :
      <p align=center><img src="Demostration and Results/ScreenShots/6.jpg" width="200" height="350" > &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/7.jpg" width="200" height="350" ></p>
  <li>When user <b>Press "Predict" Button</b> the image is sent to <b>Segmentation Algorithm</b> which segments the lungs from the image. <b>After segmentation of the lungs, the deep learning inference is generated</b> and diplayed on the screen.
    <p align=center><img src="Demostration and Results/ScreenShots/8.jpg" width="200" height="350" > &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/9.jpg" width="200" height="350" ></p>
    <li>The last screen appears after the inference resulted in more than 50% covid-19 prediction. Now <b>the image is passed to Heatmap Generation Algorithm</b> which is ScoreCAM. This step generates the heatmap and augments it on the lungs, after which <b>Using the trackbar and checkbox, heatmap gradient colour and mask of lungs can be adjusted</b> respectively. Further this modified and augmented image can be saved by one click.
      <p align=center><img src="Demostration and Results/ScreenShots/10.jpg" width="200" height="350" > &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/11.jpg" width="200" height="350" >&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/12.jpg" width="200" height="350" > &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<img src="Demostration and Results/ScreenShots/13.jpg" width="200" height="350" ></p>

# Technical stack
<ul>
<li>Usage of android studio to build the app with <b>java
    and XML</b> as backend languages.
  <li><b>OpenCV Library</b> for Lungs Segmentation with custom Segmentation Algorithm and other Image Processing Tasks.
    <li><b>Nd4j Scientific Computing Library for JVM along with OpenCV JAVA</b> for Heavy Image processing Tasks and handling N-Dimensional arrays while generating Heatmaps.
 <li><b>Keras library with Tensorflow</b> backend for the creation
    of deep learning model to detect Covid-19 in Chest CT Images.
<li>Python Scripting on Google Colab/Jupyter Notebook
    for the model to be trained. And integration of the <b>.tflite</b> file 
    which reduces the size of AI model and hence the app.
<li>Numpy and Pandas Libraries for ease of Model Training
  
