Android Ultra Photo Selector [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android--Ultra--Photo--Selector-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1527)
=============================
Select images from Android devices made easy :-) In preview you can also Zoom images. You can select images from different albums. I am using UIL, so you can configure image caching your own way, if you want to change.
on your mobil phone. Selection image result is also preserved. See **AndroidManifest.xml** for more details.

		Intent intent = new Intent(this, PhotoSelectorActivity.class);
        intent.putExtras(PhotoSelectorActivity.getBundle(maxImage, showCamera));
        startActivityForResult(intent, REQUEST_SELECT);

		@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		if (requestCode == REQUEST_SELECT) {// selected image
			if (data != null && data.getExtras() != null) {
				@SuppressWarnings("unchecked")
				List<PhotoModel> photos = (ArrayList<PhotoModel>) data.getExtras().getSerializable("photos");
			}
		}


> - **Select Images from Album**


![Select Images](https://github.com/tream/Android-Ultra-Photo-Selector/blob/master/media/image1.jpg)

> - **Browsing all device folders that have images**


![Browse Albums](https://github.com/tream/Android-Ultra-Photo-Selector/blob/master/media/image2.jpg)

> - **Preview & zoom selected images**


![Preview selected Images](https://github.com/tream/Android-Ultra-Photo-Selector/blob/master/media/image3.jpg)


