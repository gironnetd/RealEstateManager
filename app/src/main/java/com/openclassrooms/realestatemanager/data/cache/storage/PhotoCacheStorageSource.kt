package com.openclassrooms.realestatemanager.data.cache.storage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.util.Constants
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

class PhotoCacheStorageSource constructor(private val cacheDir: File) {

    fun count(): Single<Int> { return findAllPhotos().map { photos -> photos.size } }

    fun count(propertyId: String): Single<Int> {
        return findPhotosByPropertyId(propertyId).map { photos -> photos.size }
    }

    fun savePhoto(photo: Photo): Completable {
        return Completable.create { emitter ->
            photo.bitmap?.let { bitmap ->
                val outputStream = FileOutputStream(
                    File(photo.storageLocalDatabase(cacheDir, true)), true)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                emitter.onComplete()
            } ?: emitter.onError(NullPointerException("bitmap for photo ${photo.id} is null"))
        }
    }

    fun savePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            photo.bitmap?.let { savePhoto(photo) } ?: Completable.complete()
        }
    }

    fun findPhotoById(id: String): Single<Bitmap> {
        return Single.create { emitter ->
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            if(photoDir.name.contains(id)) {
                                photoDir.listFiles()?.let { photoFile ->
                                    val bitmap = BitmapFactory.decodeFile(photoFile[0].toString())
                                    emitter.onSuccess(bitmap)
                                    //bitmaps.add(bitmap)
                                }
                            }
                        }
                    } ?: emitter.onError(NullPointerException("Photos cacheDir for Property: ${propertyDir.name} is null"))
                }
                //emitter.onSuccess(bitmaps)
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }

    fun findPhotosByIds(ids: List<String>): Single<List<Bitmap>> {
        return Single.create { emitter ->
            val bitmaps: MutableList<Bitmap> = mutableListOf()
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            if(ids.any { it in photoDir.name }) {
                                photoDir.listFiles()?.let { photoFile ->
                                    val bitmap = BitmapFactory.decodeFile(photoFile[0].toString())
                                    bitmaps.add(bitmap)
                                }
                            }
                        }
                    } ?: emitter.onError(NullPointerException("Photos cacheDir for Property: ${propertyDir.name} is null"))
                }
                emitter.onSuccess(bitmaps)
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }

    fun findAllPhotos(): Single<List<Bitmap>> {
        return Single.create { emitter ->
            val bitmaps: MutableList<Bitmap> = mutableListOf()
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            photoDir.listFiles()?.let { photoFile ->
                                val bitmap = BitmapFactory.decodeFile(photoFile[0].toString())
                                bitmaps.add(bitmap)
                            }
                        }
                    } ?: emitter.onError(NullPointerException("Photos cacheDir for Property: ${propertyDir.name} is null"))
                }
                emitter.onSuccess(bitmaps)
            } ?: emitter.onSuccess(emptyList())
        }
    }

    fun findPhotosByPropertyId(propertyId: String): Single<List<Bitmap>> {
        return Single.create { emitter ->
            val bitmaps: MutableList<Bitmap> = mutableListOf()
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    if(propertyDir.name == propertyId) {
                        val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                        photosDir.listFiles()?.let { photosDirListFiles ->
                            photosDirListFiles.forEach { photoDir ->
                                photoDir.listFiles()?.let { photoFile ->
                                    val bitmap = BitmapFactory.decodeFile(photoFile[0].toString())
                                    bitmaps.add(bitmap)
                                }
                            }
                            emitter.onSuccess(bitmaps)
                        } ?: emitter.onError(NullPointerException("Photos cacheDir for Property: ${propertyDir.name} is null"))
                    }
                }
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }

    fun updatePhoto(photo: Photo): Completable {
        return Completable.create { emitter ->
            photo.bitmap?.let { bitmap ->
                File(photo.storageLocalDatabase(cacheDir, true)).delete()
                val outputStream = FileOutputStream(
                    File(photo.storageLocalDatabase(cacheDir, true)), true)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                emitter.onComplete()
            } ?: emitter.onError(NullPointerException("bitmap for photo ${photo.id} is null"))
        }
    }

    fun updatePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            photo.bitmap?.let { updatePhoto(photo) } ?: Completable.complete()
        }
    }

    fun deletePhotosByIds(ids: List<String>): Completable {
        return Completable.create { emitter ->
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            if(ids.any { it in photoDir.name }) {
                                photoDir.listFiles()?.let { photoDirListFiles ->
                                    photoDirListFiles.forEach { photoFile ->
                                        photoFile.delete()
                                    }
                                    photoDir.delete()
                                }
                                emitter.onComplete()
                            }
                        }
                    } ?: emitter.onError(NullPointerException("Photos cacheDir for Property: ${propertyDir.name} is null"))
                }
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }

    fun deletePhotos(photos: List<Photo>): Completable {
        return Completable.create { emitter ->
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            if(photos.map { it.id }.any { it in photoDir.name }) {
                                photoDir.listFiles()?.let { photoDirListFiles ->
                                    photoDirListFiles.forEach { photoFile ->
                                        photoFile.delete()
                                    }
                                    photoDir.delete()
                                }
                            }
                        }
                    } ?: emitter.onError(NullPointerException("Photos cacheDir for Property: ${propertyDir.name} is null"))
                }
                emitter.onComplete()
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }

    fun deleteAllPhotos(): Completable {
        return Completable.create { emitter ->
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            photoDir.listFiles()?.let { photoDirListFiles ->
                                photoDirListFiles.forEach { photoFile ->
                                    photoFile.delete()
                                }
                                photoDir.delete()
                            }
                        }
                        photosDir.delete()
                        propertyDir.delete()
                    }
                }
                propertiesDir.delete()
                emitter.onComplete()
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }

    fun deletePhotoById(id: String): Completable {
        return Completable.create { emitter ->
            val propertiesDir = File(cacheDir.absolutePath, Constants.PROPERTIES_COLLECTION)
            propertiesDir.listFiles()?.let { propertiesDirListFiles ->
                propertiesDirListFiles.forEach { propertyDir ->
                    val photosDir = File(propertyDir, Constants.PHOTOS_COLLECTION)
                    photosDir.listFiles()?.let { photosDirListFiles ->
                        photosDirListFiles.forEach { photoDir ->
                            if(photoDir.name.contains(id)) {
                                photoDir.listFiles()?.let { photoDirListFiles ->
                                    photoDirListFiles.forEach { photoFile ->
                                        photoFile.delete()
                                    }
                                    photoDir.delete()
                                }
                                emitter.onComplete()
                            }
                        }
                    } ?: emitter.onError(NullPointerException("Photos cacheDir for Directory: ${propertyDir.name} is null"))
                }
            } ?: emitter.onError(NullPointerException("Properties cacheDir is null"))
        }
    }
}