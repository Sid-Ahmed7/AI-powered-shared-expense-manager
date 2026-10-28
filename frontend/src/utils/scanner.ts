import { Camera } from '@capacitor/camera';

async function webPathToFile(webPath: string | undefined, name: string): Promise<File | null> {
  if (!webPath) return null;
  try {
    const res = await fetch(webPath);
    const blob = await res.blob();
    return new File([blob], name, { type: blob.type || 'image/jpeg' });
  } catch {
    return null;
  }
}

export async function takePhotoNative(): Promise<File | null> {
  try {
    const photo = await Camera.takePhoto({ quality: 80 });
    return webPathToFile(photo.webPath, 'receipt.jpg');
  } catch {
    return null;
  }
}

export async function pickFromGalleryNative(): Promise<File | null> {
  try {
    const result = await Camera.chooseFromGallery({ allowMultipleSelection: false });
    return webPathToFile(result.results[0]?.webPath, 'receipt.jpg');
  } catch {
    return null;
  }
}
