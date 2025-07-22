# Release Notes

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased (5.1.0-SNAPSHOT)

## [5.0.0] - 2025-07-21
* First jakarta release
* Update parent to latest release for calendar client. Add eventreg values to globals and remove from form. Add event reg admin url to system properties. 
* Add missing license files 
* Don't add EventState to session. Also remove form as a field. 
* Rename classes and methods to refer to collection rather than calendar. No code changes other than deletion of a duplicate (now) method. 
* Revert partially - some changes. 
* Use a more fluent approach for responses 
* Pass a calendar object instead of path. We already have the calendar in all cases. 
* Use request method to fetch collection. 
* Remove some yellow flags 
* Tidy up callback code a little 
* Update message to help debugging 
* Changes to exception handling - try to catch client disappearing. 
* Try to avoid broken pipe messages in logs 
* Use icu classes for date formatting. java.util.DateFormat inserts characters like Narrow No-Break Space into result. 
* Add url for eventreg ws to jsp. 
* Refactoring to clarify things for hlc and llc work. 
* Refactoring to clarify things for hlc and llc work. 
* Remove unnecessary class 
* Move hlc into separate module 
* Add some annotations for struts 

## [4.1.3] - 2025-02-06
* Upgrade to struts 6.7.0 in advance of move to jakarta 
* Mostly remove saveOrUpdate - a hibernate only feature - and replace with jpa compliant calls to add or update. 
* Remove all the blob refs from the source. Mapping now handles byte[] to blob 
* Add start of openjpa implementation. Add new database exception and move db related exceptions into new package. Get rid of HibException and use above. Remove some db interface features we never used: 
  * getBlob(InputStream val, long length); 
  * reattach 
  * webMode which used hibernate specific flushmode 
  * createNoFlushQuery 
  * getQueryString 
  * saveOrUpdateCopy 
  * restore 
  * reAttach 
  * lockRead 
  * lockUpdate 
* Move response classes and ToString into bw-base module. 
* Move most of the exceptions into the new bw-base module. 
* Remove all unnecessary refs to CalFacadeException. Move error codes out of CalFacadeException into CalFacadeErrorCode. 
* Add an attachment if set 
* Add url for new event reg web service to configs. Use it in client to post notifications. 

## [4.1.2] - 2024-11-27
* Update library versions
* Move old stuff out of github.io docs directory 
* Mostly fixes to the new code. Replies not getting through for a number of reasons. Rename some methods. Try to clarify handling of xproperties 
* Many more changes related to participants. More use of SchedulingOwner 
* Renamed BwParticipants SchedulingInfo. 
* Mostly switch to using BwParticipants and Attendee objects to manipulate event attendees and participants. 
* Fix errors in AdminBwModule - misnamed and misused fields ,eamt we had events created with wrong creator. 
* Missed setting of global flag 
* Flush cached views every few mins 
* Mostly cosmetic changes. Also minor fixes to contentName support. 
* Remove unnecessary references to CalFacadeException 
* Removed confirmationId - not used. Removed savedEvent - not used. 
* Moved currentCalSuite, calSuiteName into globals. 
* Moved currentTab into globals. Moved ref out of all jsp files and into header.jsp 
* Move oneGroup and adminGroupName into globals. 
* Add yearVals attribute to selectDate and selectDateTime 
* Move submissionsroot and workflowroot and associated flags 
* Remove unused yearvals 
* Move today out of form into globals 
* Remove context from form. Not used. 
* Remove schemeHostPort from form. Not used. 
* Move BwForm tag out of struts package into taglib. Was there for transition from struts1. Get rid of all refs to bwhtml 
* Further changes to remove dependencies on form objects. 
* Use session variable to access error and message objects. 
* Remove unused serialization variables. Was moved into modules long ago. 
* Fix bad expression - updates not working 
* Added a refresh operation to the synch engine and added associated code to the client side. 
* More changes to detect multi-tab use in event add/update 

## [4.1.1] - 2024-04-03
* Update library versions
* Add dependencies to the project parent to ensure all transitive dependencies during builds of this project resolve to project version and NOT the version set in bedework-parent

## [4.1.0] - 2024-04-01
* Update library versions
* Update parent and add code to check that we don't have a uid or recurrenceId mismatch.
* Add method to handle end type parameter
* Next stage in removing the bw-xml module.
  * Move synch xml
  * Added feature packs for the 2 wsdl deployments
  * Removed the xml feature pack from the build.
  * removed the xml modules from the build.
* Remove some throws clauses. Don't wrap exceptions in RunTimeException. Try not invalidating the session. There are multiple requests and this may cause errors in the one that got through
* Watch for calfacade closed and try to be less noisy. This seems to happen when users double click or something like that
* If we get hit by multiple requests and 1 gets service unavailable (times out waiting) the session object gets cleared and other requests will fail with no session. Avoid a big trace and just emit a warning.
* Remove code that always causes npe. Try to make less noise when far end drops connection.
* Use the original file for content - not the processed image. Resulted in http length errors.
* ...

## [4.0.2] - 2022-03-06
* Reinstate code removed in error - required for notifications in UI
* Output extra info about principal

## [4.0.1] - 2022-02-15
* Fix up issues with splitting out of rw/admin features. Broke user client.

## [4.0.0] - 2022-02-12
* Many changes up to this point. github log may be best reference.

