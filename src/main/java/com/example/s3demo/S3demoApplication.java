package com.example.s3demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;


@SpringBootApplication
public class S3demoApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(S3demoApplication.class, args);

		AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "check (~/.aws/credentials), and is in valid format.",
                    e);
        }
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("us-east-2")
                .build();

            String bucketName = "my-first-s3-bucket-" + UUID.randomUUID();
            String key = "MyObjectKey";

            System.out.println("===========================================");
            System.out.println("Getting Started with Amazon S3");
            System.out.println("===========================================\n");

            try {
                /*
                 * Create a new S3 bucket
                 */
                System.out.println("Creating bucket " + bucketName + "\n");
                s3.createBucket(bucketName);

                /*
                 * List the buckets in your account
                 */
                System.out.println("Listing buckets");
                for (Bucket bucket : s3.listBuckets()) {
                    System.out.println(" - " + bucket.getName());
                }
                System.out.println();

                //upload
                System.out.println("Uploading a new object to S3 from a file\n");
                s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));

                //download
                System.out.println("Downloading an object");
                S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
                System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
                displayTextInputStream(object.getObjectContent());

                //List Objects
                System.out.println("Listing objects");
                ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix("My"));
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + "  " +
                                       "(size = " + objectSummary.getSize() + ")");
                }
                System.out.println();

                // delete object 
                
                System.out.println("Deleting an object\n");
                s3.deleteObject(bucketName, key);

                // deleting bucket
                
                System.out.println("Deleting bucket " + bucketName + "\n");
                s3.deleteBucket(bucketName);
            } catch (AmazonServiceException ase) {
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                
                System.out.println("Error Message: " + ace.getMessage());
            }
        }

        /*
         * Creates a temporary 
         */
        private static File createSampleFile() throws IOException {
            File file = File.createTempFile("aws-java-sdk-", ".txt");
            file.deleteOnExit();

            Writer writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write("abcdefghijklmnopqrstuvwxyz\n");
            writer.write("01234567890112345678901234\n");
            writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
            writer.write("01234567890112345678901234\n");
            writer.write("abcdefghijklmnopqrstuvwxyz\n");
            writer.close();

            return file;
        }

        /**
         * Displays the contents 
         */
        private static void displayTextInputStream(InputStream input) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while (true) {
                String line = reader.readLine();
                if (line == null) break;

                System.out.println("    " + line);
            }
            System.out.println();
        }
}
